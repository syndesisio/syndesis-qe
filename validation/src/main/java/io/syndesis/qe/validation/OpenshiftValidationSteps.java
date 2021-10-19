package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

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
import io.syndesis.qe.resource.impl.SFTP;
import io.syndesis.qe.resource.impl.WildFlyS2i;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscalerBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenshiftValidationSteps {
    @Then("check that pod {string} logs contain string {string}")
    public void checkPodHasInLog(String podPartialName, String expectedText) {
        assertThat(OpenShiftUtils.getPodLogs(podPartialName)).containsIgnoringCase(expectedText);
    }

    @Given("deploy FTP server")
    public void deployFTPServer() {
        ResourceFactory.create(FTP.class);
    }

    @Given("deploy SFTP server")
    public void deploySFTPServer() {
        ResourceFactory.create(SFTP.class);
    }

    @Given("deploy Kudu")
    public void deployKudu() {
        ResourceFactory.create(Kudu.class);
    }

    @Given("clean MySQL server")
    public void cleanMySQLServer() {
        ResourceFactory.destroy(MySQL.class);
    }

    @Given("deploy MySQL server")
    public void deployMySQLServer() {
        ResourceFactory.create(MySQL.class);
    }

    @Given("deploy ActiveMQ broker")
    public void deployAMQBroker() {
        ResourceFactory.create(AMQ.class);
    }

    @Given("create ActiveMQ accounts")
    public void addActiveMQAccounts() {
        ResourceFactory.get(AMQ.class).addAccounts();
    }

    @Given("deploy Kafka broker")
    public void deployKafka() {
        ResourceFactory.create(Kafka.class);
    }

    @Given("create Kafka accounts")
    public void createKafkaAccounts() {
        ResourceFactory.get(Kafka.class).addAccounts();
    }

    @Given("deploy HTTP endpoints")
    public void deployHTTPEndpoints() {
        ResourceFactory.create(HTTPEndpoints.class);
    }

    @Given("create HTTP accounts")
    public void createHTTPAccounts() {
        ResourceFactory.get(HTTPEndpoints.class).addAccounts();
    }

    @Given("deploy IRC server")
    public void deployIRCServer() {
        ResourceFactory.create(IRC.class);
    }

    @Given("create IRC account")
    public void createIRCAccount() {
        ResourceFactory.get(IRC.class).addAccount();
    }

    @Given("deploy OData v4 server")
    public void deployODataServer() {
        WildFlyS2i wildFlyS2i = ResourceFactory.get(WildFlyS2i.class);
        wildFlyS2i.setAppName("odata");
        wildFlyS2i.setGitURL("https://github.com/syndesisio/syndesis-qe-olingo-sample-service.git");
        wildFlyS2i.setBranch(null);
        ResourceFactory.create(WildFlyS2i.class);
    }

    @Given("deploy MongoDB 3.6 database")
    public void deployMongoDB36() {
        ResourceFactory.create(MongoDb36.class);
    }

    @Given("create MongoDB account")
    public void createMongoDBAccount() {
        ResourceFactory.get(MongoDb36.class).addAccount();
    }

    @Given("create Kudu account")
    public void createKuduAccount() {
        ResourceFactory.get(Kudu.class).createAccount();
    }

    @Given("^create OData( HTTPS)? v4 credentials$")
    public void createODataV4HttpCredentials(String https) {
        ResourceFactory.get(WildFlyS2i.class).createODataV4Account(https != null && !https.isEmpty());
    }

    @Given("^create OData v2 credentials$")
    public void createODataV2HttpCredentials() {
        ResourceFactory.get(WildFlyS2i.class).createODataV2Account();
    }

    @Given("wait until {string} pod is reloaded")
    public void waitUntilPodIsReloaded(String podName) {
        try {
            OpenShiftWaitUtils.waitForPodIsReloaded(podName);
        } catch (InterruptedException | TimeoutException e) {
            InfraFail.fail(e.getMessage());
        }
    }

    @Then("check that the pod {string} is not redeployed by server")
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

    @Then("check that the pod {string} is not redeployed by its deployment")
    public void checkThatPodIsNotRedeployedByDeployment(String deploymentName) {
        Optional<Deployment> deployment = OpenShiftUtils.getDeploymentByPartialName(deploymentName);
        assertThat(deployment).isPresent();
        int currentNr = deployment.get().getMetadata().getGeneration().intValue();
        waitForStateCheckInterval();

        // Check that deployment generation is not higher
        assertThat(OpenShiftUtils.deploymentExists(
            p -> p.getMetadata().getName().contains(deploymentName),
            p -> p.getMetadata().getGeneration().intValue() > currentNr))
            .as("There should be no deployment with higher generation number")
            .isFalse();
    }

    @Then("^check that the pod \"([^\"]*)\" (has|has not) appeared$")
    public void checkThatPodExist(String podPartialName, String shouldExist) {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName(podPartialName);
        if (shouldExist.contains("not")) {
            assertThat(pod).isEmpty();
        } else {
            assertThat(pod).isPresent();
        }
    }

    @When("wait until {string} pod is running")
    public void waitForPodIsRunning(String podPartialName) {
        OpenShiftWaitUtils.waitUntilPodIsRunning(podPartialName);
    }

    @When("wait for state check interval")
    public void waitForStateCheckInterval() {
        // Wait for a state check so that server can figure out that something is not right + a bit more for spawning a new pod
        TestUtils.sleepIgnoreInterrupt((TestConfiguration.stateCheckInterval() + 60) * 1000L);
    }

    @When("edit replicas count for deployment config {string} to {int}")
    public void editReplicasCount(String dcName, int replicas) {
        OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).edit().editSpec().withReplicas(replicas).endSpec().done();
    }

    @When("add following variables to the {string} deployment config:")
    public void addEnvVarsToDc(String dcName, DataTable vars) {
        Map<String, String> content = vars.asMap(String.class, String.class);
        for (Map.Entry<String, String> keyValue : content.entrySet()) {
            OpenShiftUtils.updateEnvVarInDeploymentConfig(dcName, keyValue.getKey(), keyValue.getValue());
        }
    }

    @Then("check that the deployment config {string} contains variables:")
    public void checkDcEnvVars(String dcName, DataTable vars) {
        assertThat(OpenShiftUtils.getInstance().getDeploymentConfigEnvVars(dcName))
            .containsAllEntriesOf(vars.asMap(String.class, String.class));
    }

    @Then("^check that there (?:is|are) (\\d+) pods? for integration \"([^\"]*)\"$")
    public void checkPodsForIntegration(int count, String integrationName) {
        assertThat(OpenShiftUtils.getInstance().getPods("i-" + integrationName)).hasSize(count);
    }

    @When("change deployment strategy for {string} deployment config to {string}")
    public void changeDeploymentStrategy(String dcName, String strategy) {
        OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).edit().editSpec().editStrategy().withType(strategy).endStrategy().endSpec()
            .done();
    }

    @Then("chech that the deployment strategy for {string} deployment config is {string}")
    public void checkDeploymentStrategy(String dcName, String strategy) {
        assertThat(OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).get().getSpec().getStrategy().getType())
            .isEqualTo(strategy);
    }

    @When("create HPA for deployment config {string} with {int} replicas")
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

    @Then("^check that deployment config \"([^\"]*)\" (does|does not) exist$")
    public void checkDeploymentConfig(String dcName, String shouldExist) {
        DeploymentConfig dc = OpenShiftUtils.getInstance().getDeploymentConfig(dcName);
        if ("does".equals(shouldExist)) {
            assertThat(dc).isNotNull();
        } else {
            assertThat(dc).isNull();
        }
    }

    @Then("^check that service \"([^\"]*)\" (does|does not) exist$")
    public void checkService(String serviceName, String shouldExist) {
        Service service = OpenShiftUtils.getInstance().getService(serviceName);
        if ("does".equals(shouldExist)) {
            assertThat(service).isNotNull();
        } else {
            assertThat(service).isNull();
        }
    }

    @Then("check that SAR check is disabled")
    public void checkSar() {
        DeploymentConfig dc = OpenShiftUtils.getInstance().getDeploymentConfig("syndesis-oauthproxy");
        Optional<String> sarArg = dc.getSpec().getTemplate().getSpec().getContainers().get(0).getArgs().stream()
            .filter(arg -> arg.contains("--openshift-sar")).findFirst();
        assertThat(sarArg).isNotPresent();
    }

    @Then("check that SAR check is enabled for namespace {string}")
    public void checkSar(String namespace) {
        if (namespace.isEmpty()) {
            namespace = TestConfiguration.openShiftNamespace();
        }
        DeploymentConfig dc = OpenShiftUtils.getInstance().getDeploymentConfig("syndesis-oauthproxy");
        Optional<String> sarArg = dc.getSpec().getTemplate().getSpec().getContainers().get(0).getArgs().stream()
            .filter(arg -> arg.contains("--openshift-sar")).findFirst();
        assertThat(sarArg).isPresent();
        assertThat(sarArg.get()).contains("\"namespace\":\"" + namespace + "\"");
    }

    @When("set resources for deployment config {string}")
    public void setResourceValues(String dcName, DataTable dataTable) {
        DeploymentConfig dc = OpenShiftUtils.getInstance().getDeploymentConfig(dcName);
        Map<String, Quantity> currentLimits = dc.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits();
        Map<String, Quantity> currentRequests = dc.getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests();
        Map<String, Quantity> limits = currentLimits == null ? new HashMap<>() : new HashMap<>(currentLimits);
        Map<String, Quantity> requests = currentRequests == null ? new HashMap<>() : new HashMap<>(currentRequests);
        for (List<String> l : dataTable.asLists()) {
            if ("limits".equals(l.get(0))) {
                limits.put(l.get(1), new Quantity(l.get(2)));
            } else {
                requests.put(l.get(1), new Quantity(l.get(2)));
            }
        }
        // @formatter:off
        OpenShiftUtils.getInstance().deploymentConfigs().withName(dcName).edit()
            .editSpec()
                .editTemplate()
                    .editSpec()
                        .editFirstContainer()
                            .editResources()
                                .withLimits(limits)
                                .withRequests(requests)
                            .endResources()
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
        .done();
        // @formatter:on
    }

    @Then("^verify that the deployment config \"([^\"]*)\" has the (cpu|memory) (limits|requests) set to \"([^\"]*)\"$")
    public void verifyResourceValues(String dcName, String which, String what, String value) {
        ResourceRequirements resources = OpenShiftUtils.getInstance().getDeploymentConfig(dcName).getSpec().getTemplate().getSpec().getContainers().get(0).getResources();
        Map<String, Quantity> check = "limits".equals(what) ? resources.getLimits() : resources.getRequests();
        assertThat(check).isNotNull();
        assertThat(check).containsKey(which);
        assertThat(check.get(which).getAmount() + check.get(which).getFormat()).isEqualTo(value);
    }
}
