package io.syndesis.qe.utils;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.IntegrationOverviewEndpoint;
import io.syndesis.qe.model.IntegrationOverview;
import io.syndesis.qe.utils.dballoc.DBAllocation;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author jknetl
 */
@Slf4j
public final class TestUtils {
    private static final String VERSION_ENDPOINT = "/api/v1/version";

    /**
     * Waits until a predicate is true or timeout exceeds.
     *
     * @param predicate predicate
     * @param supplier supplier of values to test by predicate
     * @param unit TimeUnit for timeout
     * @param timeout how long to wait for event
     * @param sleepUnit TimeUnit of sleep interval between tests
     * @param sleepTime how long to wait between individual tests (in miliseconds)
     * @param <T> Type of tested value by a predicate
     * @return True if predicate become true within a timeout, otherwise returns false.
     */
    public static <T> boolean waitForEvent(Predicate<T> predicate, Supplier<T> supplier, TimeUnit unit, long timeout, TimeUnit sleepUnit,
        long sleepTime) {
        final long start = System.currentTimeMillis();
        long elapsed = 0;
        while (!predicate.test(supplier.get()) && unit.toMillis(timeout) >= elapsed) {
            try {
                sleepUnit.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.debug("Interupted while sleeping", e);
            } finally {
                elapsed = System.currentTimeMillis() - start;
                System.gc();
            }
        }

        return predicate.test(supplier.get());
    }

    public static boolean waitForPublishing(IntegrationOverviewEndpoint e, IntegrationOverview i, TimeUnit unit, long timeout) {
        return waitForState(e, i, IntegrationDeploymentState.Published, unit, timeout);
    }

    public static boolean waitForUnpublishing(IntegrationOverviewEndpoint e, IntegrationOverview i, TimeUnit unit, long timeout) {
        return waitForState(e, i, IntegrationDeploymentState.Unpublished, unit, timeout);
    }

    /**
     * Waits until integration reaches a specified state or timeout exceeds.
     *
     * @param e Integration endpoint to obtain current state
     * @param i integration
     * @param state desired integration state
     * @param unit Time unit
     * @param timeout timeout
     * @return True if integration is activated within a timeout. False otherwise.
     */
    public static boolean waitForState(IntegrationOverviewEndpoint e, IntegrationOverview i, IntegrationDeploymentState state, TimeUnit unit,
        long timeout) {
        return waitForEvent(
            // integration -> integration.getCurrentStatus().orElse(IntegrationDeploymentState.Pending) == state,
            integration -> integration.getCurrentState() == state,
            () -> getIntegration(e, i.getId()).orElse(i),
            unit,
            timeout,
            TimeUnit.SECONDS,
            10
        );
    }

    private static Optional<IntegrationOverview> getIntegration(IntegrationOverviewEndpoint e, String integrationId) {
        return Optional.of(e.getOverview(integrationId));
    }

    public static LocalPortForward createLocalPortForward(String podName, int remotePort, int localPort) {
        try {
            final Pod podToForward = OpenShiftUtils.getInstance().getAnyPod("syndesis.io/component", podName);
            return OpenShiftUtils.portForward(podToForward, remotePort, localPort);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex.getMessage() + ". Probably Syndesis is not in the namespace.");
        }
    }

    public static LocalPortForward createLocalPortForward(Pod pod, int remotePort, int localPort) {
        return OpenShiftUtils.portForward(pod, remotePort, localPort);
    }

    public static boolean isDcDeployed(String dcName) {
        DeploymentConfig dc = OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).get();
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
     * Creates map from objects.
     *
     * @param values key1, value1, key2, value2, ...
     * @return map instance with objects
     */
    public static Map<String, String> map(Object... values) {
        final HashMap<String, String> rc = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }

    public static void sleepIgnoreInterrupt(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            log.error("Sleep was interrupted!");
            e.printStackTrace();
        }
    }

    public static void waitFor(BooleanSupplier condition, int checkIntervalInSeconds, int timeoutInSeconds, String errorMessage) {
        try {
            OpenShiftWaitUtils.waitFor(condition, checkIntervalInSeconds * 1000L, timeoutInSeconds * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail(errorMessage, e);
        }
    }

    /**
     * Same as {@link #waitFor(BooleanSupplier, int, int, String)}, but does not fail in case of timeout,
     * instead it has return value that determines whether wait condition ended with success
     * @return true if condition was satisfied, false otherwise
     */
    public static boolean waitForNoFail(BooleanSupplier condition, int checkIntervalInSeconds, int timeoutInSeconds) {
        try {
            OpenShiftWaitUtils.waitFor(condition, checkIntervalInSeconds * 1000L, timeoutInSeconds * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            return false;
        }

        return true;
    }

    public static void sleepForJenkinsDelayIfHigher(int delayInSeconds) {
        log.debug("sleeping for " + delayInSeconds + " seconds");
        sleepIgnoreInterrupt(Math.max(TestConfiguration.getJenkinsDelay(), delayInSeconds) * 1000);
    }

    public static void setDatabaseCredentials(String connectionName, DBAllocation dbAllocation) {
        Map<String, String> allocPropertiesMap = dbAllocation.getAllocationMap();

        TestUtils.transhipExternalProperties(connectionName, allocPropertiesMap);
    }

    /**
     * This is method for transhipping externally dynamicaly generated connection data(Database, etc.) into
     * io.syndesis.qe.accounts.Account properties.
     *
     * @param connectionName name of the connection
     * @param sourceMap source map
     */
    private static void transhipExternalProperties(String connectionName, Map<String, String> sourceMap) {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(connectionName);

        Account account;

        if (!optional.isPresent()) {
            account = new Account();
            account.setService(connectionName);
            AccountsDirectory.getInstance().setAccount(connectionName, account);
        } else {
            account = optional.get();
        }

        Map<String, String> properties = account.getProperties();
        if (properties == null) {
            account.setProperties(new HashMap<>());
            properties = account.getProperties();
        }
        switch (account.getService()) {
            case "oracle12":
            case "mysql":
                properties.put("url", sourceMap.get("db.jdbc_url"));
                properties.put("user", sourceMap.get("db.username"));
                properties.put("password", sourceMap.get("db.password"));
                properties.put("schema", sourceMap.get("db.schema"));
                log.debug("UPDATED ACCOUNT {} PROPERTIES:", account.getService());
                properties.forEach((key, value) -> log.debug("Key: *{}*, value: *{}*", key, value));
                break;
        }
    }

    /**
     * Checks if the currently logged in user is a cluster admin.
     *
     * @return true/false
     */
    public static boolean isUserAdmin() {
        return isUserAdmin(getCurrentUser());
    }

    /**
     * Checks if the given user has admin rights.
     *
     * @param user user
     * @return true/false
     */
    public static boolean isUserAdmin(String user) {
        try {
            return OpenShiftUtils.getInstance().clusterRoleBindings().inAnyNamespace().list().getItems().stream()
                .filter(crb -> crb.getMetadata().getName().startsWith("cluster-admin") && crb.getUserNames() != null)
                .anyMatch(
                    crb -> crb.getUserNames().contains(user)
                );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the currently logged in user in the binary client.
     *
     * @return username for the current user
     */
    public static String getCurrentUser() {
        return OpenShiftUtils.binary().execute("whoami").trim();
    }

    /**
     * Check if the test is running on jenkins.
     *
     * @return true/false
     */
    public static boolean isJenkins() {
        return System.getenv("WORKSPACE") != null;
    }

    /**
     * Checks if we are testing productized bits.
     *
     * @return true/false
     */
    public static boolean isProdBuild() {
        return TestConfiguration.syndesisVersion().contains("redhat");
    }

    /**
     * Prints pods using oc binary client.
     *
     * @return output of oc get pods
     */
    public static String printPods() {
        // Use oc client directly, as it has nice output
        final String output = OpenShiftUtils.binary().execute(
            "get", "pods", "-n", TestConfiguration.openShiftNamespace()
        );
        log.error(output);
        return output;
    }

    /**
     * Replaces the text in file and writes it back to file.
     *
     * @param f file to use
     * @param regex regex
     * @param replacement replacement
     */
    public static void replaceInFile(File f, String regex, String replacement) {
        try {
            String content = FileUtils.readFileToString(f, "UTF-8");
            FileUtils.write(f, content.replaceAll(regex, replacement), "UTF-8", false);
        } catch (IOException e) {
            fail("Unable to replace content in " + f.getAbsolutePath(), e);
        }
    }

    /**
     * Gets the syndesis version using the version endpoint.
     *
     * @return syndesis version
     */
    public static String getSyndesisVersion() {
        RestUtils.reset();
        Request request = new Request.Builder()
            .url(RestUtils.getRestUrl() + VERSION_ENDPOINT)
            .header("Accept", "text/plain")
            .build();
        try {
            return new OkHttpClient.Builder().build().newCall(request).execute().body().string();
        } catch (IOException e) {
            log.error("Unable to get version from " + VERSION_ENDPOINT);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves the useful info to a log file.
     */
    public static void saveDebugInfo() {
        final String fileName = "error-" + new Date().getTime() + ".log";
        final String content = TestUtils.printPods() + "\n" + OpenShiftUtils.getPodLogs("syndesis-server");
        try {
            FileUtils.writeStringToFile(new File("log/" + fileName), content, "UTF-8");
            log.error("Wrote server debug stuff to " + fileName);
        } catch (IOException ex) {
            log.error("Unable to write string to file: ", ex);
        }
    }

    /**
     * Checks if the OpenShift version is 3.x
     *
     * @return true/false
     */
    public static boolean isOpenshift3() {
        // on our openstack clusters 1.11+ is 3.11 and 1.14+ is 4.2
        VersionInfo version = OpenShiftUtils.getInstance().getVersion();
        return version != null && version.getMinor().contains("11+");
    }
}
