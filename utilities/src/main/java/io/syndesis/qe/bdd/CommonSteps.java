package io.syndesis.qe.bdd;

import io.syndesis.qe.Component;
import io.syndesis.qe.templates.FtpTemplate;

import io.syndesis.qe.utils.TestUtils;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.templates.AmqTemplate;
import io.syndesis.qe.templates.SyndesisTemplate;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.LogCheckerUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.SampleDbConnectionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {

    @Given("^clean default namespace")
    public void cleanNamespace() {
        OpenShiftUtils.getInstance().cleanAndAssert();
    }

    @Given("^clean all builds")
    public void cleanBuilds() {
        OpenShiftUtils.getInstance().getBuildConfigs().forEach(OpenShiftUtils.getInstance()::deleteBuildConfig);
        OpenShiftUtils.getInstance().getBuilds().forEach(OpenShiftUtils.getInstance()::deleteBuild);
    }

    @When("^deploy Syndesis from template")
    public void deploySyndesis() {
        SyndesisTemplate.deploy();
    }

    @Then("^wait for Syndesis to become ready")
    public void waitForSyndeisis() {
        EnumSet<Component> components = EnumSet.allOf(Component.class);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        components.forEach(c -> {
            Runnable runnable = () ->
                    OpenShiftUtils.xtf().waiters()
                            .areExactlyNPodsReady(1, "component", c.getName())
                            .interval(TimeUnit.SECONDS, 20)
                            .timeout(TimeUnit.MINUTES, 12)
                            .assertEventually();
            executorService.submit(runnable);
        });

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                Assertions.fail("Syndesis wasn't initilized in time");
            }
        } catch (InterruptedException e) {
            Assertions.fail("Syndesis wasn't initilized in time");
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
                    Assertions.assertThat(patternsInLogs).containsOnly(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Assertions.assertThat(build.getStatus().getPhase()).isEqualTo("Complete");
            // % 1_000L is there to parse OpenShift ms format
            Assertions.assertThat(build.getStatus().getDuration() % 1_000L).isLessThan(duration * 60 * 1000);
        } else {
            Assertions.fail("No build found for integration with name " + sanitizedName);
        }
    }

    @Given("^deploy AMQ broker and add accounts$")
    public void deployAMQBroker() {
        AmqTemplate.deploy();
    }

    @Given("^execute SQL command \"([^\"]*)\"$")
    public void executeSql(String sqlCmd) {
        new DbUtils(SampleDbConnectionManager.getConnection()).executeSQLGetUpdateNumber(sqlCmd);
    }

    @Given("^clean TODO table$")
    public void cleanTodoTable() {
        new DbUtils(SampleDbConnectionManager.getConnection()).deleteRecordsInTable("TODO");
    }

    @Given("^clean application state")
    public void resetState() {
        boolean resetStatus = TestUtils.waitForEvent(rc -> rc == 204, () -> TestSupport.getInstance().resetDbWithResponse(), TimeUnit.MINUTES, 5, TimeUnit.SECONDS, 10);
        Assertions.assertThat(resetStatus).as("Reset failed in given timeout").isTrue();
    }

    @Given("^deploy FTP server$")
    public void deployFTPServier() {
        FtpTemplate.deploy();
    }

    @Given("^clean FTP server$")
    public void cleanFTPServier() {
        FtpTemplate.cleanUp();
    }
}
