package io.syndesis.qe.utils;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenShift utils.
 *
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

}
