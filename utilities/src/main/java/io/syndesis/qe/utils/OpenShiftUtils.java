package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

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

    public enum ResourceType {
        DEPLOYMENT_CONFIG,
        DEPLOYMENT
    }

    public static OpenShift getInstance() {
        if (xtfUtils == null) {
            xtfUtils = useAdminUser();
        }
        return xtfUtils;
    }

    public static OpenShiftBinary binary() {
        if (binary == null) {
            log.debug("Downloading OpenShift binary");
            TestUtils.withRetry(() -> {
                try {
                    binary = OpenShifts.masterBinary(TestConfiguration.openShiftNamespace());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }, 3, 30000, "Unable to get OpenShift binary");
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
        try {
            r.run();
        } finally {
            xtfUtils = useAdminUser();
        }
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

    public static LocalPortForward portForward(Pod pod, int remotePort, int localPort) {
        return getPodResource(pod).portForward(remotePort, localPort);
    }

    private static PodResource<Pod, DoneablePod> getPodResource(Pod pod) {
        if (pod.getMetadata().getNamespace() != null) {
            return getInstance().pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName());
        } else {
            return getInstance().pods().withName(pod.getMetadata().getName());
        }
    }

    public static Optional<Pod> getPodByPartialName(String partialName) {
        List<Pod> pods = findPodsByPredicates(
            p -> p.getMetadata().getName().contains(partialName),
            p -> !p.getMetadata().getName().contains("deploy"),
            p -> !p.getMetadata().getName().contains("build")
        );
        return pods.size() > 0 ? Optional.of(pods.get(0)) : Optional.empty();
    }

    public static Optional<Deployment> getDeploymentByPartialName(String partialName) {
        List<Deployment> deployments = findDeploymentsByPredicates(
            p -> p.getMetadata().getName().contains(partialName)
        );
        return deployments.size() > 0 ? Optional.of(deployments.get(0)) : Optional.empty();
    }

    public static int extractPodSequenceNr(Pod pod) {
        String podFullName = pod.getMetadata().getName();
        Pattern regex = Pattern.compile(".*-(\\d+)-[a-z0-9]{5}$");
        Matcher m = regex.matcher(podFullName);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            InfraFail.fail("Unable to parse number from " + podFullName);
        }
        return -1;
    }

    public static String getIntegrationLogs(String integrationName) {
        return getPodLogs(integrationName.replaceAll("[\\s_]", "-").toLowerCase());
    }

    public static String getPodLogs(String podPartialName) {
        // pod has to be in running state because pod in ContainerCreating state causes exception
        OpenShiftWaitUtils.waitUntilPodIsRunning(podPartialName);
        Optional<Pod> pod = getPodByPartialName(podPartialName);
        if (pod.isPresent()) {
            String logText = OpenShiftUtils.getInstance().getPodLog(pod.get());
            assertThat(logText).isNotEmpty();
            return logText;
        } else {
            InfraFail.fail("No pod found for pod name: " + podPartialName);
        }
        // this can not happen due to assert - make idea happy that it can't be null
        return "";
    }

    /**
     * Creates the resource using binary oc client.
     *
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
     * Returns all Deployments that match the given predicates.
     *
     * @param predicates predicates to match
     * @return list of deployments that match the given predicates.
     */
    @SafeVarargs
    public static List<Deployment> findDeploymentsByPredicates(Predicate<Deployment>... predicates) {
        Stream<Deployment> deploymentStream = OpenShiftUtils.getInstance().apps().deployments().list().getItems().stream();
        for (Predicate<Deployment> predicate : predicates) {
            deploymentStream = deploymentStream.filter(predicate);
        }
        return deploymentStream.collect(Collectors.toList());
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
     * Checks if the deployment with given predicates exist.
     *
     * @param predicates predicates to match
     * @return true/false
     */
    @SafeVarargs
    public static boolean deploymentExists(Predicate<Deployment>... predicates) {
        Stream<Deployment> deploymentStream = OpenShiftUtils.getInstance().apps().deployments().list().getItems().stream();
        for (Predicate<Deployment> predicate : predicates) {
            deploymentStream = deploymentStream.filter(predicate);
        }
        return deploymentStream.findAny().isPresent();
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

    public static boolean hasPodIssuesPullingImage(Pod pod) {
        return pod.getStatus().getContainerStatuses().stream().anyMatch(status ->
            status.getState().getWaiting() != null
                && ("ImagePullBackOff".equals(status.getState().getWaiting().getReason()) ||
                "ErrImagePull".equals(status.getState().getWaiting().getReason())));
    }

    public static boolean dcContainsEnv(String dcName, String envName) {
        return OpenShiftUtils.getInstance().getDeploymentConfig(dcName)
            .getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()
            .stream().anyMatch(envVar -> envVar.getName().equals(envName));
    }

    public static boolean envInDcContainsValue(String dcName, String envName, String envValue) {
        EnvVar envVar = OpenShiftUtils.getInstance().getDeploymentConfig(dcName)
            .getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().stream().filter(env -> env.getName().equals(envName)).findAny()
            .get();
        return envValue.equals(envVar.getValue());
    }

    public static LocalPortForward createLocalPortForward(String podName, int remotePort, int localPort) {
        try {
            List<Pod> podsByPredicates = findPodsByPredicates(
                p -> p.getMetadata().getName().contains(podName),
                p -> "Running".equals(p.getStatus().getPhase())
            );
            if (podsByPredicates.size() == 0) {
                log.warn("No pods in running state with name " + podName + " found!");
                return null;
            }
            return portForward(podsByPredicates.get(0), remotePort, localPort);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex.getMessage() + ". Probably Syndesis is not in the namespace.");
        }
    }

    public static LocalPortForward createLocalPortForward(Pod pod, int remotePort, int localPort) {
        return portForward(pod, remotePort, localPort);
    }

    public static boolean isDcDeployed(String dcName) {
        DeploymentConfig dc = getInstance().deploymentConfigs().withName(dcName).get();
        return dc != null && dc.getStatus().getReadyReplicas() != null && dc.getStatus().getReadyReplicas() > 0;
    }

    public static void terminateLocalPortForward(LocalPortForward lpf) {
        if (lpf == null) {
            return;
        }
        if (lpf.isAlive()) {
            try {
                lpf.close();
            } catch (IOException ex) {
                log.error("Error: " + ex);
            }
        } else {
            log.info("Local Port Forward already closed.");
        }
    }

    /**
     * Checks if the OpenShift version is 3.x
     *
     * @return true/false
     */
    public static boolean isOpenshift3() {
        // minishift returns null, check differently
        if (isMinishift()) {
            return true;
        }
        // on our openstack clusters 1.11+ is 3.11 and 1.14+ is 4.2
        VersionInfo version = getInstance().getVersion();
        return version != null && version.getMinor().contains("11+");
    }

    public static boolean isCrc() {
        return TestConfiguration.openShiftUrl().matches("^.*api\\.crc.*:6443$");
    }

    public static boolean isMinishift() {
        return TestConfiguration.openShiftUrl().matches("^.*192\\.168\\.\\d{1,3}\\.\\d{1,3}:8443$");
    }

    public static void scale(String name, int replicas, ResourceType type) {
        log.info("Scaling {} to {}", name, replicas);
        TestUtils.withRetry(() -> {
            try {
                switch (type) {
                    case DEPLOYMENT:
                        OpenShiftUtils.getInstance().apps().deployments().withName(name).scale(replicas);
                        break;
                    case DEPLOYMENT_CONFIG:
                        OpenShiftUtils.getInstance().scale(name, replicas);
                        break;
                    default:
                        fail("Unknown resource type");
                }
                return true;
            } catch (KubernetesClientException e) {
                log.debug("Caught exception while scaling", e);
                return false;
            }
        }, 3, 30000L, "Unable to set replicas for " + name + " to " + replicas);
        try {
            OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPods(name, replicas), 2 * 60 * 1000);
        } catch (InterruptedException | TimeoutException e) {
            log.debug("Waited too long for " + name + " to get to " + replicas + " replicas", e);
        }
    }
}
