package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * OpenShift utils.
 * <p>
 * Sep 8, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class OpenShiftUtils {

    private static OpenShiftUtil xtfUtils = null;

    public static OpenShiftUtil getInstance() {
        if (xtfUtils == null) {
            new OpenShiftUtils();
        }
        return xtfUtils;
    }

    public static OpenShiftUtil xtf() {
        return getInstance();
    }

    private OpenShiftUtils() {
        if (xtfUtils == null) {
            final OpenShiftConfigBuilder openShiftConfigBuilder = new OpenShiftConfigBuilder()
                    .withMasterUrl(TestConfiguration.openShiftUrl())
                    .withTrustCerts(true)
                    .withRequestTimeout(120_000)
                    .withNamespace(TestConfiguration.openShiftNamespace());
            if (!TestConfiguration.openShiftToken().isEmpty()) {
                //if token is provided, lets use it
                //otherwise f8 client should be able to leverage ~/.kube/config or mounted secrets
                openShiftConfigBuilder.withOauthToken(TestConfiguration.openShiftToken());
            }
            xtfUtils = new OpenShiftUtil(openShiftConfigBuilder.build());
        }
    }

    public static NamespacedOpenShiftClient client() {
        return getInstance().client();
    }

    public static Route createRestRoute(String openShiftNamespace, String urlSuffix) {
        final Route route = new RouteBuilder()
                .withNewMetadata()
                .withName(Component.SERVER.getName())
                .endMetadata()
                .withNewSpec()
                .withPath("/api").withHost("rest-" + openShiftNamespace + "." + urlSuffix)
                .withWildcardPolicy("None")
                .withNewTls()
                .withTermination("edge")
                .endTls()
                .withNewTo()
                .withKind("Service").withName(Component.SERVER.getName())
                .endTo()
                .endSpec()
                .build();
        return client().resource(route).createOrReplace();
    }

    public static LocalPortForward portForward(Pod pod, int remotePort, int localPort) {
        return getPodResource(pod).portForward(remotePort, localPort);
    }

    private static PodResource<Pod, DoneablePod> getPodResource(Pod pod) {
        if (pod.getMetadata().getNamespace() != null) {
            return client().pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName());
        } else {
            return client().pods().withName(pod.getMetadata().getName());
        }
    }

    public static Optional<Pod> getPodByPartialName(String partialName) {
        Optional<Pod> oPod = OpenShiftUtils.getInstance().getPods().stream()
                .filter(p -> p.getMetadata().getName().contains(partialName))
                .filter(p -> !p.getMetadata().getName().contains("deploy"))
                .filter(p -> !p.getMetadata().getName().contains("build"))
                .findFirst();
        return oPod;
    }

    public static int extractPodSequenceNr(Pod pod) {
        String podFullName = pod.getMetadata().getName();
        String[] pole = podFullName.split("-");
        return Integer.parseInt(pole[2]);
    }

    public static String getIntegrationLogs(String integrationName) {
        return getPodLogs(integrationName.replaceAll("[\\s_]", "-").toLowerCase());
    }

    public static String getPodLogs(String podPartialName) {
        Optional<Pod> integrationPod = OpenShiftUtils.getInstance().getPods().stream()
                .filter(p -> !p.getMetadata().getName().contains("build"))
                .filter(p -> !p.getMetadata().getName().contains("deploy"))
                .filter(p -> p.getMetadata().getName().contains(podPartialName)).findFirst();
        if (integrationPod.isPresent()) {
            String logText = OpenShiftUtils.getInstance().getPodLog(integrationPod.get());
            assertThat(logText)
                    .isNotEmpty();
            return logText;
        } else {
            fail("No pod found for pod name: " + podPartialName);
        }
        //this can not happen due to assert
        return null;
    }

    /**
     * Some of the resources can't be created by f8 client, therefore we manually post them to corresponding endpoint.
     * @param kind kind of the resource
     * @param o object instance
     */
    public static void create(String kind, Object o) {
        try {
            final String content = Serialization.jsonMapper().writeValueAsString(o);
            StringBuilder url = new StringBuilder();
            String[] kinds = new String[] {"serviceaccount", "role", "rolebinding", "clusterrole", "clusterrolebinding", "deployment"};
            if (StringUtils.equalsAnyIgnoreCase(kind, kinds)) {
                url.append("/api")
                        .append(kind.toLowerCase().equals("serviceaccount") ? "" : "s")
                        .append("/")
                        .append(((HasMetadata)o).getApiVersion())
                        .append("/namespaces/")
                        .append(TestConfiguration.openShiftNamespace())
                        .append("/")
                        .append(kind.toLowerCase())
                        .append("s");
            } else {
                // This can be created by the client, so create it
                getInstance().createResources((HasMetadata) o);
                return;
            }

            Response response = invokeApi(url.toString(), content);
            // 409 means that the resource already exists - this is the case for clusterrole / clusterbinding that are tied to the whole
            // cluster obviously - therefore it is ok to continue with 409
            if (response.code() != 409) {
                assertThat(response.code()).isGreaterThanOrEqualTo(200);
                assertThat(response.code()).isLessThan(300);
            }
        } catch (IOException e) {
            fail("Unable to create resource", e);
        }
    }

    /**
     * Invoke openshift's API. Only part behind master url is necessary and the path must start with slash.
     * @param url api path
     * @param body body to send as JSON
     * @return response object
     */
    public static Response invokeApi(String url, String body) {
        url = TestConfiguration.openShiftUrl() + url;
        log.debug(url);
        Response response = HttpUtils.doPostRequest(
                url,
                body,
                "application/json",
                Headers.of("Authorization", "Bearer " + OpenShiftUtils.client().getConfiguration().getOauthToken())
        );
        log.debug("Response code: " + response.code());
        try {
            log.debug("Response: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
