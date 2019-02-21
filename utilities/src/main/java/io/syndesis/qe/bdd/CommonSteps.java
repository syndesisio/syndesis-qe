package io.syndesis.qe.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.templates.SyndesisTemplate;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.LogCheckerUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

@Slf4j
public class CommonSteps {
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    @Given("^clean default namespace")
    public void cleanNamespace() {
        undeploySyndesis();
        OpenShiftUtils.client().apps().statefulSets().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.client().extensions().deployments().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.client().serviceAccounts().withName("syndesis-oauth-client").delete();
        OpenShiftUtils.getInstance().cleanAndAssert();
        OpenShiftUtils.xtf().getTemplates().forEach(OpenShiftUtils.xtf()::deleteTemplate);
    }

    @Given("^clean all builds")
    public void cleanBuilds() {
        OpenShiftUtils.getInstance().getBuildConfigs().forEach(OpenShiftUtils.getInstance()::deleteBuildConfig);
        OpenShiftUtils.getInstance().getBuilds().forEach(OpenShiftUtils.getInstance()::deleteBuild);
    }

    @When("^deploy Syndesis$")
    public void deploySyndesis() {
        SyndesisTemplate.deploy();
    }

    @When("^deploy Syndesis from template$")
    public void deploySyndesisFromTemplate() {
        SyndesisTemplate.deployUsingTemplate();
    }

    @Then("^wait for Syndesis to become ready")
    public void waitForSyndesis() {
        waitFor(true);
    }

    /**
     * Undeploys deployed syndesis resources.
     */
    public static void undeploySyndesis() {
        if (OpenShiftUtils.getPodByPartialName("syndesis-operator").isPresent()) {
            for (String s : customResourceNames()) {
                undeployCustomResource(s);
            }
            CommonSteps.waitForUndeployment();
        }
        OpenShiftUtils.client().customResourceDefinitions().list().getItems()
                .stream().filter(crd -> crd.getMetadata().getName().contains("syndesis.io"))
                .forEach(crd -> OpenShiftUtils.client().customResourceDefinitions().delete(crd));
    }

    /**
     * Gets the Set of deployed syndesis custom resources.
     * @return set containing names of deployed syndesis custom resources.
     */
    private static Set<String> customResourceNames() {
        final Set<String> names = new HashSet<>();
        final String url = "/apis/syndesis.io/v1alpha1/namespaces/" + TestConfiguration.openShiftNamespace() + "/" + TestConfiguration.customResourcePlural();
        String responseBody = OpenShiftUtils.invokeApi(
                HttpUtils.Method.GET,
                url,
                null,
                Headers.of("Accept", "application/json")
        ).getBody();
        JSONArray items = new JSONObject(responseBody).getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            names.add(((JSONObject)items.get(i)).getJSONObject("metadata").getString("name"));
        }
        return names;
    }

    /**
     * Undeploys syndesis custom resource using openshift API.
     * @param name custom resource name
     */
    private static void undeployCustomResource(String name) {
        log.info("Undeploying Syndesis custom resource \"" + name + "\"");
        final String url = "/apis/syndesis.io/v1alpha1/namespaces/" + TestConfiguration.openShiftNamespace() + "/" + TestConfiguration.customResourcePlural() + "/" + name;
        OpenShiftUtils.invokeApi(HttpUtils.Method.DELETE, url, null, null);
    }

    /**
     * Waits for syndesis to be undeployed.
     */
    public static void waitForUndeployment() {
        waitFor(false);
    }

    /**
     * Waits for syndesis deployment / undeployment.
     * @param deploy true if waiting for deploy, false otherwise
     */
    private static void waitFor(boolean deploy) {
        final int timeout = TestUtils.isJenkins() ? 20 : 12;
        EnumSet<Component> components = EnumSet.allOf(Component.class);

        ExecutorService executorService = Executors.newFixedThreadPool(components.size());
        components.forEach(c -> {
            Runnable runnable = () -> {
                if (deploy) {
                    OpenShiftUtils.xtf().waiters()
                            .areExactlyNPodsReady(1, "syndesis.io/component", c.getName())
                            .interval(TimeUnit.SECONDS, 20)
                            .timeout(TimeUnit.MINUTES, timeout)
                            .assertEventually();
                } else {
                    OpenShiftUtils.xtf().waiters()
                            .areExactlyNPodsRunning(0, "syndesis.io/component", c.getName())
                            .interval(TimeUnit.SECONDS, 20)
                            .timeout(TimeUnit.MINUTES, timeout)
                            .assertEventually();
                }
            };
            executorService.submit(runnable);
        });

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                fail((deploy ? "Syndesis wasn't initialized in time" : "Syndesis wasn't undeployed in time"));
            }
        } catch (InterruptedException e) {
            fail((deploy ? "Syndesis wasn't initialized in time" : "Syndesis wasn't undeployed in time"));
        }
    }

    @Then("^verify s2i build of integration \"([^\"]*)\" was finished in duration (\\d+) min$")
    public void verifyBuild(String integrationName, int duration) {
        String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");

        Optional<Build> s2iBuild = OpenShiftUtils.getInstance().getBuilds().stream().filter(b -> b.getMetadata().getName().contains(sanitizedName)).findFirst();

        if (s2iBuild.isPresent()) {
            Build build = s2iBuild.get();
            String buildPodName = build.getMetadata().getAnnotations().get("openshift.io/build.pod-name");
            Optional<Pod> buildPod = OpenShiftUtils.getInstance().getPods().stream().filter(p -> p.getMetadata().getName().equals(buildPodName)).findFirst();
            if (buildPod.isPresent()) {
                try {
                    boolean[] patternsInLogs = LogCheckerUtils.findPatternsInLogs(buildPod.get(), Pattern.compile(".*Downloading: \\b.*"));
                    assertThat(patternsInLogs).containsOnly(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            assertThat(build.getStatus().getPhase()).isEqualTo("Complete");
            // % 1_000L is there to parse OpenShift ms format
            assertThat(build.getStatus().getDuration() % 1_000L).isLessThan(duration * 60 * 1000);
        } else {
            fail("No build found for integration with name " + sanitizedName);
        }
    }

    @Given("^clean application state")
    public void resetState() {
        //check that postgreSQl connection has been created
        int i = 0;
        while (i < 10) {
            TestSupport.getInstance().resetDB();
            Optional<Connection> optConnection = connectionsEndpoint.list().stream().filter(s -> s.getName().equals("PostgresDB")).findFirst();
            if (optConnection.isPresent()) {
                return;
            }
            i++;
        }
        fail("Default PostgresDB connection has not been created, please contact engineering!");
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
                    OpenShiftUtils.xtf().waiters()
                            .areExactlyNPodsReady(1, "syndesis.io/app", "todo")
                            .interval(TimeUnit.SECONDS, 20)
                            .timeout(TimeUnit.MINUTES, 12)
                            .assertEventually();
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
}
