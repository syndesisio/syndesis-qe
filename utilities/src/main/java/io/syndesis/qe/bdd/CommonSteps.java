package io.syndesis.qe.bdd;

import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
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
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {

	@Given("^clean default namespace")
	public void cleanNamespace() {
		OpenShiftUtils.getInstance().cleanProject();
	}

	@When("^deploy Syndesis from template")
	public void deploySyndesis() {
		SyndesisTemplate.deploy();
	}

	@Then("^wait for Syndesis to become ready")
	public void waitForSyndeisis() {
		try {
			OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("component", "syndesis-rest"));
		} catch (InterruptedException | TimeoutException e) {
			log.error("Wait for syndesis-rest failed ", e);
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

	@Given("^deploy AMQ broker$")
	public void deployAMQBroker() throws Throwable {
		AmqTemplate.deploy();
	}

	@Given("^execute SQL command \"([^\"]*)\"$")
	public void executeSql(String sqlCmd) {
		DbUtils dbUtils = new DbUtils(SampleDbConnectionManager.getInstance().getConnection());
		dbUtils.readSqlOnSampleDb(sqlCmd);
		SampleDbConnectionManager.getInstance().closeConnection();
	}

	@Given("^clean TODO table$")
	public void cleanTodoTable() {
		DbUtils dbUtils = new DbUtils(SampleDbConnectionManager.getInstance().getConnection());
		dbUtils.deleteRecordsInTable("TODO");
		SampleDbConnectionManager.getInstance().closeConnection();
	}

	@Given("^clean application state")
	public void resetState() {
		int responseCode = TestSupport.getInstance().resetDbWithResponse();
		Assertions.assertThat(responseCode).isEqualTo(204);
	}
}
