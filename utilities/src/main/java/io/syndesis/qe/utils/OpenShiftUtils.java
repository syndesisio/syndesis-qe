package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;

import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

/**
 * OpenShift utils.
 * <p>
 * Sep 8, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class OpenShiftUtils {
    private static OpenShift xtfUtils = null;
    private static OpenShiftBinary binary = null;

    public static OpenShift getInstance() {
        if (xtfUtils == null) {
            xtfUtils = useAdminUser();
        }
        return xtfUtils;
    }

    public static OpenShiftBinary binary() {
        if (binary == null) {
            binary = OpenShifts.masterBinary(TestConfiguration.openShiftNamespace());
        }
        return binary;
    }

    /**
     * Runs the given code with the permissions of a regular user(without admin rights, specified by the syndesis ui username property)
     *
     * @param r code to run
     */
    public static void asRegularUser(Runnable r) {
        xtfUtils = useRegularUser();
        r.run();
        xtfUtils = useAdminUser();
    }

    /**
     * Creates a new client with the regular user.
     */
    private static OpenShift useRegularUser() {
        return OpenShift.get(
            TestConfiguration.openShiftUrl(),
            TestConfiguration.openShiftNamespace(),
            TestConfiguration.syndesisUsername(),
            TestConfiguration.syndesisPassword()
        );
    }

    /**
     * Creates a new client with the admin user.
     */
    private static OpenShift useAdminUser() {
        return OpenShift.get(
            TestConfiguration.openShiftUrl(),
            TestConfiguration.openShiftNamespace(),
            TestConfiguration.adminUsername(),
            TestConfiguration.adminPassword()
        );
    }

    /**
     * @deprecated use OpenshiftUtils.getInstance()
     */
    @Deprecated
    public static NamespacedOpenShiftClient client() {
        return getInstance();
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
        return OpenShiftUtils.getInstance().getPods().stream()
            .filter(p -> p.getMetadata().getName().contains(partialName))
            .filter(p -> !p.getMetadata().getName().contains("deploy"))
            .filter(p -> !p.getMetadata().getName().contains("build"))
            .findFirst();
    }

    public static int extractPodSequenceNr(Pod pod) {
        String podFullName = pod.getMetadata().getName();
        Pattern regex = Pattern.compile(".*-(\\d+)-[a-z0-9]{5}$");
        Matcher m = regex.matcher(podFullName);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            fail("Unable to parse number from " + podFullName);
        }
        return -1;
    }

    public static String getIntegrationLogs(String integrationName) {
        return getPodLogs(integrationName.replaceAll("[\\s_]", "-").toLowerCase());
    }

    public static String getPodLogs(String podPartialName) {
        // pod has to be in running state because pod in ContainerCreating state causes exception
        OpenShiftWaitUtils.waitUntilPodIsRunning(podPartialName);
        Optional<Pod> integrationPod = getPodByPartialName(podPartialName);
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

    public static boolean arePodLogsEmpty(String podPartialName) {
        // pod has to be in running state because pod in ContainerCreating state causes exception
        OpenShiftWaitUtils.waitUntilPodIsRunning(podPartialName);

        Optional<Pod> integrationPod = getPodByPartialName(podPartialName);
        return integrationPod.map(pod -> OpenShiftUtils.getInstance().getPodLog(pod).isEmpty()).orElse(true);
    }

    /**
     * Invoke openshift's API. Only part behind master url is necessary and the path must start with slash.
     * @param method HTTP method to use
     * @param url api path
     * @param body body to send as JSON
     * @return response object
     */
    public static HTTPResponse invokeApi(HttpUtils.Method method, String url, String body) {
        return invokeApi(method, url, body, null);
    }

    /**
     * Invoke openshift's API. Only part behind master url is necessary and the path must start with slash.
     * @param method HTTP method to use
     * @param url api path
     * @param body body to send as JSON
     * @param headers headers to send, can be null
     * @return response object
     */
    public static HTTPResponse invokeApi(HttpUtils.Method method, String url, String body, Headers headers) {
        url = TestConfiguration.openShiftUrl() + url;
        if (headers == null) {
            headers = Headers.of("Authorization", "Bearer " + OpenShiftUtils.getInstance().getConfiguration().getOauthToken());
        } else {
            if (headers.get("Authorization") == null) {
                headers = headers.newBuilder().add("Authorization", "Bearer " + OpenShiftUtils.getInstance().getConfiguration().getOauthToken()).build();
            }
        }

        log.debug(url);
        HTTPResponse response = null;
        switch (method) {
            case GET: {
                response = HttpUtils.doGetRequest(url, headers);
                break;
            }
            case POST: {
                response = HttpUtils.doPostRequest(url, body, headers);
                break;
            }
            case PUT: {
                response = HttpUtils.doPutRequest(url, body, headers);
                break;
            }
            case DELETE: {
                response = HttpUtils.doDeleteRequest(url, headers);
                break;
            }
            default: {
                fail("Unable to use specified HTTP Method!");
            }
        }

        return response;
    }

    /**
     * Creates the resource using binary oc client.
     * @param resource path to resource file to use with -f
     */
    public static void create(String resource) {
        final String output = binary().execute(
                "apply",
                "--overwrite=false",
                "-n", TestConfiguration.openShiftNamespace(),
                "-f", resource
        );
        log.info(output);
    }

    /**
     * Deletes the resource using binary oc client.
     *
     * @param resource resource to delete
     */
    public static void delete(String resource) {
        final String output = binary().execute(
            "delete",
            "-n", TestConfiguration.openShiftNamespace(),
            "-f", resource
        );
        log.info(output);
    }

    /**
     * Returns all pods that match the given predicates.
     *
     * @param predicates predicates to match
     * @return list of pods that match the given predicates.
     */
    @SafeVarargs
    public static List<Pod> findPodsByPredicates(Predicate<Pod>... predicates) {
        Stream<Pod> podStream = OpenShiftUtils.getInstance().pods().list().getItems().stream();
        for (Predicate<Pod> predicate : predicates) {
            podStream = podStream.filter(predicate);
        }
        return podStream.collect(Collectors.toList());
    }

    /**
     * Returns the first pod that matches the predicates, or fails if none matches.
     *
     * @param predicates predicates to match
     * @return first pod that matches the predicates
     */
    @SafeVarargs
    public static Pod getPod(Predicate<Pod>... predicates) {
        List<Pod> pods = findPodsByPredicates(predicates);
        assertThat(pods).size().isGreaterThan(0);
        if (pods.size() > 1) {
            log.warn("There were multiple pods found with given predicate, returning the first one found");
        }
        return pods.get(0);
    }

    /**
     * Gets the first integration pod that matches the given predicates.
     *
     * @param integrationName integration name
     * @param predicates predicates to match
     * @return first pod that matches the predicates
     */
    @SafeVarargs
    public static Pod getIntegrationPod(String integrationName, Predicate<Pod>... predicates) {
        return getPod(withIntegrationName(integrationName, predicates));
    }

    /**
     * Checks if the pod with given predicates exist.
     *
     * @param predicates predicates to match
     * @return true/false
     */
    @SafeVarargs
    public static boolean podExists(Predicate<Pod>... predicates) {
        Stream<Pod> podStream = OpenShiftUtils.getInstance().pods().list().getItems().stream();
        for (Predicate<Pod> predicate : predicates) {
            podStream = podStream.filter(predicate);
        }
        return podStream.findAny().isPresent();
    }

    /**
     * Checks if integration pod with given predicates exist.
     *
     * @param integrationName integration name
     * @param predicates predicates to match
     * @return true/false
     */
    @SafeVarargs
    public static boolean integrationPodExists(String integrationName, Predicate<Pod>... predicates) {
        return podExists(withIntegrationName(integrationName, predicates));
    }

    /**
     * Adds the filter for pod name to the array of predicates.
     *
     * @param integrationName integration name
     * @param predicates array of predicates
     * @return array of predicates with a predicate for integration pod
     */
    private static Predicate<Pod>[] withIntegrationName(String integrationName, Predicate[] predicates) {
        Predicate[] preds = Arrays.copyOf(predicates, predicates.length + 1);
        preds[preds.length - 1] = (Predicate<Pod>) p -> p.getMetadata().getName().contains(integrationName.replaceAll(" ", "-"));
        return preds;
    }

    /**
     * Gets the pod, or returns an empty optional if the pod is not present
     *
     * @param labelName pod label name
     * @param labelValue pod label value
     * @return optional
     */
    public static Optional<Pod> getAnyPod(String labelName, String labelValue) {
        try {
            return Optional.of(OpenShiftUtils.getInstance().getAnyPod(labelName, labelValue));
        } catch (IllegalArgumentException ex) {
            // When the pod is not there yet, the getAnyPod throws IllegalArgumentException, so ignore it
            return Optional.empty();
        }
    }

    public static void updateEnvVarInDeploymentConfig(String dcName, String key, String value) {
        DeploymentConfig dc = getInstance().getDeploymentConfig(dcName);

        List<EnvVar> vars = dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();

        Optional<EnvVar> var = vars.stream().filter(a -> a.getName().equalsIgnoreCase(key)).findFirst();
        if (var.isPresent()) {
            var.get().setValue(value);
        } else {
            log.warn("Variable " + key + " not found in " + dcName + " deployment config, creating it.");
            vars.add(new EnvVar(key, value, null));
        }

        dc.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(vars);
        getInstance().updateDeploymentconfig(dc);
    }
}
