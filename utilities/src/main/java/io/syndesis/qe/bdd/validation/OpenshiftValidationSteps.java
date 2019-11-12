package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.AMQ;
import io.syndesis.qe.resource.impl.FTP;
import io.syndesis.qe.resource.impl.HTTPEndpoints;
import io.syndesis.qe.resource.impl.IRC;
import io.syndesis.qe.resource.impl.Kafka;
import io.syndesis.qe.resource.impl.Kudu;
import io.syndesis.qe.resource.impl.MongoDb36;
import io.syndesis.qe.resource.impl.MySQL;
import io.syndesis.qe.resource.impl.PublicOauthProxy;
import io.syndesis.qe.resource.impl.WildFlyS2i;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.fabric8.kubernetes.api.model.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenshiftValidationSteps {
    @Then("^wait until mysql database starts$")
    public void waitUntilDatabaseStarts() {
        ResourceFactory.get(MySQL.class).waitUntilMysqlIsReady();
    }

    @Then("^check that pod \"([^\"]*)\" logs contain string \"([^\"]*)\"$")
    public void checkPodHasInLog(String podPartialName, String expectedText) {
        assertThat(OpenShiftUtils.getPodLogs(podPartialName))
            .containsIgnoringCase(expectedText);
    }

    @Given("^deploy FTP server$")
    public void deployFTPServer() {
        ResourceFactory.create(FTP.class);
    }

    @Given("^deploy Kudu$")
    public void deployKudu() {
        ResourceFactory.create(Kudu.class);
    }

    @Given("^clean MySQL server$")
    public void cleanMySQLServer() {
        ResourceFactory.destroy(MySQL.class);
    }

    @Given("^deploy MySQL server$")
    public void deployMySQLServer() {
        ResourceFactory.create(MySQL.class);
    }

    @Given("^deploy ActiveMQ broker$")
    public void deployAMQBroker() {
        ResourceFactory.create(AMQ.class);
    }

    @Given("^deploy Kafka broker and add account$")
    public void deployKafka() {
        ResourceFactory.create(Kafka.class);
    }

    @Given("^deploy HTTP endpoints")
    public void deployHTTPEndpoints() {
        ResourceFactory.create(HTTPEndpoints.class);
    }

    @Given("^deploy IRC server")
    public void deployIRCServer() {
        ResourceFactory.create(IRC.class);
    }

    @Given("^deploy public oauth proxy$")
    public void deployApiOauthProxy() {
        ResourceFactory.create(PublicOauthProxy.class);
    }

    @Given("^deploy OData server$")
    public void deployODataServer() {
        WildFlyS2i wildFlyS2i = ResourceFactory.get(WildFlyS2i.class);
        wildFlyS2i.setAppName("odata");
        wildFlyS2i.setGitURL("https://github.com/syndesisio/syndesis-qe-olingo-sample-service.git");
        wildFlyS2i.setBranch(null);
        wildFlyS2i.deploy();
    }

    @Given("^deploy MongoDB 3.6 database$")
    public void deployMongoDB36() {
        ResourceFactory.create(MongoDb36.class);
    }

    @Given("^wait until \"([^\"]*)\" pod is reloaded$")
    public void waitUntilPodIsReloaded(String podName) {
        try {
            OpenShiftWaitUtils.waitForPodIsReloaded(podName);
        } catch (InterruptedException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Then("^check that the pod \"([^\"]*)\" is not redeployed by server$")
    public void checkThatPodIsNotRedeployed(String podName) {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(podName);
        assertThat(pod).isPresent();
        int currentNr = OpenShiftUtils.extractPodSequenceNr(pod.get());
        waitForStateCheckInterval();
        // Check that there is no pod with higher number
        assertThat(OpenShiftUtils.podExists(
            p -> p.getMetadata().getName().contains(podName),
            p -> !StringUtils.containsAny(p.getMetadata().getName(), "build", "deploy"),
            p -> OpenShiftUtils.extractPodSequenceNr(p) > currentNr))
            .as("There should be no pod with higher number")
            .isFalse();
    }

    @When("^wait for state check interval$")
    public void waitForStateCheckInterval() {
        // Wait for a state check so that server can figure out that something is not right + a bit more for spawning a new pod
        TestUtils.sleepIgnoreInterrupt((TestConfiguration.stateCheckInterval() + 60) * 1000L);
    }

    @When("^edit replicas count for deployment config \"([^\"]*)\" to (\\d)$")
    public void editReplicasCount(String dcName, int replicas) {
        OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).edit().editSpec().withReplicas(replicas).endSpec().done();
    }

    @When("^add following variables to the \"([^\"]*)\" deployment config:$")
    public void addEnvVarsToDc(String dcName, DataTable vars) {
        Map<String, String> content = vars.asMap(String.class, String.class);
        for (Map.Entry<String, String> keyValue : content.entrySet()) {
            OpenShiftUtils.updateEnvVarInDeploymentConfig(dcName, keyValue.getKey(), keyValue.getValue());
        }
    }

    @Then("^check that the deployment config \"([^\"]*)\" contains variables:$")
    public void checkDcEnvVars(String dcName, DataTable vars) {
        assertThat(OpenShiftUtils.getInstance().getDeploymentConfigEnvVars(dcName)).containsAllEntriesOf(vars.asMap(String.class, String.class));
    }

    @Then("^check that there (?:is|are) (\\d+) pods? for integration \"([^\"]*)\"$")
    public void checkPodsForIntegration(int count, String integrationName) {
        assertThat(OpenShiftUtils.getInstance().getPods("i-" + integrationName)).hasSize(count);
    }

    @When("^change deployment strategy for \"([^\"]*)\" deployment config to \"([^\"]*)\"$")
    public void changeDeploymentStrategy(String dcName, String strategy) {
        OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).edit().editSpec().editStrategy().withType(strategy).endStrategy().endSpec()
            .done();
    }

    @Then("^chech that the deployment strategy for \"([^\"]*)\" deployment config is \"([^\"]*)\"$")
    public void checkDeploymentStrategy(String dcName, String strategy) {
        assertThat(OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).get().getSpec().getStrategy().getType()).isEqualTo(strategy);
    }

    @When("^create HPA for deployment config \"([^\"]*)\" with (\\d+) replicas$")
    public void createHpaWithMinReplicas(String dcName, int replicas) {
        OpenShiftUtils.getInstance().createHorizontalPodAutoscaler(
            new HorizontalPodAutoscalerBuilder()
                .withNewMetadata().withName("test-hpa").withNamespace(TestConfiguration.openShiftNamespace()).endMetadata()
                .withNewSpec()
                .withNewScaleTargetRef()
                .withApiVersion("apps.openshift.io/v1")
                .withKind("DeploymentConfig")
                .withName(dcName)
                .endScaleTargetRef()
                .withMinReplicas(replicas)
                .withMaxReplicas(replicas)
                .endSpec()
                .build()
        );
    }
}
