package io.syndesis.qe.bdd;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.CamelK;
import io.syndesis.qe.resource.impl.DV;
import io.syndesis.qe.resource.impl.ExternalDatabase;
import io.syndesis.qe.resource.impl.Jaeger;
import io.syndesis.qe.resource.impl.PublicOauthProxy;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.PublicApiUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.xtf.core.waiting.WaiterException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    // Flag is the cluster reachability test failed, if it does, all remaining tests will be failed
    private static boolean isClusterReachable = true;

    @Given("^clean default namespace")
    public static void cleanNamespace() {
        undeploySyndesis();
        OpenShiftUtils.getInstance().templates().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().apps().statefulSets().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().serviceAccounts().withName("syndesis-oauth-client").delete();
        try {
            OpenShiftUtils.getInstance().clean();
            OpenShiftUtils.getInstance().waiters().isProjectClean().waitFor();
        } catch (WaiterException e) {
            log.warn("Project was not clean after 20s, retrying once again");
            OpenShiftUtils.getInstance().clean();
            OpenShiftUtils.getInstance().waiters().isProjectClean().waitFor();
        }
        OpenShiftUtils.getInstance().getTemplates().forEach(OpenShiftUtils.getInstance()::deleteTemplate);
    }

    @Given("^clean all builds")
    public void cleanBuilds() {
        OpenShiftUtils.getInstance().getBuildConfigs().forEach(OpenShiftUtils.getInstance()::deleteBuildConfig);
        OpenShiftUtils.getInstance().getBuilds().forEach(OpenShiftUtils.getInstance()::deleteBuild);
    }

    @When("^deploy Syndesis$")
    public static void deploySyndesis() {
        ResourceFactory.get(Syndesis.class).deploy();
        // Use this method instead of ResourceFactory#create() to get the info what went wrong
        waitForSyndesis();
    }

    @Then("^wait for Syndesis to become ready")
    public static void waitForSyndesis() {
        try {
            log.info("Waiting for Syndesis to get ready");
            OpenShiftWaitUtils.waitFor(() -> ResourceFactory.get(Syndesis.class).isReady(), 10000L, 15 * 60000L);
        } catch (Exception e) {
            TestUtils.printPods();
            log.error("Was waiting for following syndesis components:");
            Component.getAllComponents().forEach(c -> log.error("  " + c.getName()));
            log.error("Found following component pods:");
            Component.getComponentPods().forEach(p -> log.error("  " + p.getMetadata().getName()
                + " [ready: " + OpenShiftWaitUtils.isPodReady(p) + "]"));
            fail("Wait for Syndesis failed, check error logs for details.", e);
        }
    }

    @When("^deploy Camel-K$")
    public static void deployCamelK() {
        ResourceFactory.create(CamelK.class);
    }

    @Then("^wait for Camel-K to become ready$")
    public static void waitForCamelK() {
        OpenShiftUtils.getInstance().waiters()
            .areExactlyNPodsReady(1, "camel.apache.org/component", "operator")
            .interval(TimeUnit.SECONDS, 20)
            .timeout(TimeUnit.MINUTES, 5)
            .waitFor();
    }

    @When("^change runtime to (springboot|camelk)$")
    public static void changeRuntime(String runtime) {
        ResourceFactory.get(Syndesis.class).changeRuntime(runtime);
    }

    @Then("^wait for DV to become ready$")
    public void waitForDv() {
        OpenShiftUtils.getInstance().waiters()
            .areExactlyNPodsReady(1, "syndesis.io/component", "syndesis-dv")
            .interval(TimeUnit.SECONDS, 20)
            .timeout(TimeUnit.MINUTES, 5)
            .waitFor();
    }

    @When("^deploy Jaeger$")
    public void deployJaeger() {
        ResourceFactory.create(Jaeger.class);
    }

    @When("^deploy custom database$")
    public void deployDb() {
        ResourceFactory.create(ExternalDatabase.class);
    }

    @When("^deploy DV$")
    public void deployDv() {
        ResourceFactory.create(DV.class);
    }

    @When("^deploy public oauth proxy$")
    public void deployApiOauthProxy() {
        ResourceFactory.create(PublicOauthProxy.class);
    }

    @When("^undeploy Syndesis$")
    public static void undeploySyndesis() {
        ResourceFactory.get(Syndesis.class).undeployCustomResources();
        try {
            OpenShiftWaitUtils.waitFor(() -> ResourceFactory.get(Syndesis.class).isUndeployed(), 10 * 60000L);
        } catch (Exception e) {
            log.error("Was waiting until there is only operator pod or no pods");
            log.error("Found following component pods:");
            Component.getComponentPods().forEach(p -> log.error("  " + p.getMetadata().getName()));
            fail("Wait for Syndesis undeployment failed, check error logs for details.", e);
        }
    }

    @Given("^clean application state")
    public void resetState() {
        waitUntilClusterIsReachable();
        TestUtils.withRetry(() -> {
            TestSupport.getInstance().resetDB();
            return connectionsEndpoint.list().stream().anyMatch(s -> s.getName().equals("PostgresDB"));
        }, 10, 1000L, "Default PostgresDB connection has not been created");
    }

    /**
     * Performs a simple reachability check in a loop.
     * <p>
     * Waits up to 30 minutes for the cluster to be reachable. The check is done using a simple HTTP GET to the cluster api endpoint
     */
    private void waitUntilClusterIsReachable() {
        if (!isClusterReachable) {
            fail("Previous reachability test failed, skipping remaining tests.");
        }
        final int maxRetries = 30;
        int retries = 0;
        boolean isReachable = false;
        log.info("Checking if OpenShift cluster at {} is reachable.", TestConfiguration.openShiftUrl());
        while (retries < maxRetries) {
            isReachable = HttpUtils.isReachable(TestConfiguration.openShiftUrl());
            if (isReachable) {
                log.info("  Cluster at {} is reachable.", TestConfiguration.openShiftUrl());
                break;
            } else {
                log.debug("  Cluster at {} is was not reachable. Retrying in 1 minute.", TestConfiguration.openShiftUrl());
                // The test takes 15 seconds when not available
                TestUtils.sleepIgnoreInterrupt(45000L);
                retries++;
            }
        }
        if (!isReachable) {
            isClusterReachable = false;
            fail("Unable to contact OpenShift cluster after " + maxRetries + " tries.");
        }
    }

    @Then("^sleep for jenkins delay or \"([^\"]*)\" seconds")
    public void sleepForJenkinsDelay(int secs) {
        TestUtils.sleepForJenkinsDelayIfHigher(secs);
    }

    @When("^refresh server port-forward")
    public void refreshPortForward() {
        RestUtils.reset();
        RestUtils.getRestUrl();
    }

    @Then("^wait for Todo to become ready$")
    public void waitForTodo() {
        log.info("Waiting for Todo to get ready");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable runnable = () ->
            OpenShiftUtils.getInstance().waiters()
                .areExactlyNPodsReady(1, "syndesis.io/app", "todo")
                .interval(TimeUnit.SECONDS, 20)
                .timeout(TimeUnit.MINUTES, 12)
                .waitFor();
        executorService.submit(runnable);

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                fail("Todo app wasn't initilized in time");
            }
        } catch (InterruptedException e) {
            fail("Waiting for Todo app was interrupted with exception: " + e.getMessage());
        }
    }

    @When("^set up ServiceAccount for Public API$")
    public void setUpServiceAccountForPublicAPI() {
        PublicApiUtils.createServiceAccount();
    }
}
