package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.templates.AmqTemplate;
import io.syndesis.qe.templates.FtpTemplate;
import io.syndesis.qe.templates.HTTPEndpointsTemplate;
import io.syndesis.qe.templates.IrcTemplate;
import io.syndesis.qe.templates.KafkaTemplate;
import io.syndesis.qe.templates.KuduRestAPITemplate;
import io.syndesis.qe.templates.KuduTemplate;
import io.syndesis.qe.templates.MysqlTemplate;
import io.syndesis.qe.templates.PublicOauthProxyTemplate;
import io.syndesis.qe.templates.WildFlyTemplate;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

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
        MysqlTemplate.waitUntilMysqlIsReady();
    }

    @Then("^check that pod \"([^\"]*)\" logs contain string \"([^\"]*)\"$")
    public void checkPodHasInLog(String podPartialName, String expectedText) {
        assertThat(OpenShiftUtils.getPodLogs(podPartialName))
            .containsIgnoringCase(expectedText);
    }

    @Given("^deploy FTP server$")
    public void deployFTPServer() {
        FtpTemplate.deploy();
    }

    @Given("^deploy Kudu rest API$")
    public void deployKuduRestAPI() {
        KuduRestAPITemplate.deploy();
    }

    @Given("^deploy Kudu server$")
    public void deployKuduServer() {
        KuduTemplate.deploy();
    }

    @Given("^clean Kudu rest API$")
    public void cleanKuduRestAPI() {
        KuduRestAPITemplate.cleanUp();
    }

    @Given("^clean Kudu server$")
    public void cleanKuduServer() {
        KuduTemplate.cleanUp();
    }

    @Given("^clean FTP server$")
    public void cleanFTPServer() {
        FtpTemplate.cleanUp();
    }

    @Given("^clean MySQL server$")
    public void cleanMySQLServer() {
        MysqlTemplate.cleanUp();
    }

    @Given("^deploy MySQL server$")
    public void deployMySQLServer() {
        MysqlTemplate.deploy();
    }

    @Given("^deploy ActiveMQ broker$")
    public void deployAMQBroker() {
        AmqTemplate.deploy();
    }

    @Given("^deploy Kafka broker and add account$")
    public void deployKafka() {
        KafkaTemplate.deploy();
    }

    @Given("^deploy HTTP endpoints")
    public void deployHTTPEndpoints() {
        HTTPEndpointsTemplate.deploy();
    }

    @Given("^deploy IRC server")
    public void deployIRCServer() {
        IrcTemplate.deploy();
    }

    @Given("^deploy public oauth proxy$")
    public void deployApiOauthProxy() throws TimeoutException, InterruptedException {
        PublicOauthProxyTemplate.deploy();
    }

    @Given("^deploy OData server$")
    public void deployODataServer() {
        WildFlyTemplate.deploy("https://github.com/syndesisio/syndesis-qe-olingo-sample-service.git", "odata", null);
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
        assertThat(OpenShiftUtils.getInstance().pods().list().getItems().stream()
            .filter(p -> p.getMetadata().getName().contains(podName) && OpenShiftUtils.extractPodSequenceNr(p) > currentNr)
            .count())
            .as("There should be no pod with higher number")
            .isZero();
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
