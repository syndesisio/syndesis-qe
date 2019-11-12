package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeFluent;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperatorValidationSteps {
    public static final String TEST_PV_NAME = "test-pv";

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @Given("^deploy Syndesis CRD$")
    public void deployCRD() {
        ResourceFactory.get(Syndesis.class).deployCrd();
    }

    @Given("^grant permissions to user$")
    public void grantPermissions() {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        syndesis.pullOperatorImage();
        syndesis.grantPermissions();
    }

    @Given("^deploy Syndesis operator$")
    public void deployOperator() {
        ResourceFactory.get(Syndesis.class).deployOperator();
    }

    @When("^deploy Syndesis CR from file \"([^\"]*)\"")
    public void deployCrFromFile(String file) {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        try (FileInputStream fis = FileUtils.openInputStream(new File("src/test/resources/operator/" + file))) {
            syndesis.getSyndesisCrClient().create(
                TestConfiguration.openShiftNamespace(),
                syndesis.getSyndesisCrClient().load(fis)
            );
        } catch (IOException e) {
            fail("Unable to create file src/test/resources/operator/" + file, e);
        }
    }

    @Then("^check deployed syndesis version$")
    public void checkVersion() {
        assertThat(TestUtils.getSyndesisVersion()).isEqualTo(System.getProperty("syndesis.version") == null ?
            System.getProperty(TestConfiguration.SYNDESIS_INSTALL_VERSION)
            : System.getProperty("syndesis.version"));
    }

    @Then("^check that there are no errors in operator$")
    public void checkErrors() {
        assertThat(OpenShiftUtils.getPodLogs("syndesis-operator").toLowerCase()).doesNotContain("error");
    }

    @Then("^check the deployed route$")
    public void checkRoute() {
        final String file = "src/test/resources/operator/spec/routeHostname.yml";
        String expectedRoute = null;
        try {
            expectedRoute = StringUtils.substringAfter(FileUtils.readFileToString(new File(file), "UTF-8"), "routeHostname: ").trim();
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().routes().withName("syndesis").get() != null, 120000L);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().routes().withName("syndesis").get()
                .getStatus().getIngress() != null, 120000L);
        } catch (Exception e) {
            fail("Unable to check route: ", e);
        }

        assertThat(OpenShiftUtils.getInstance().routes().withName("syndesis").get().getStatus().getIngress().get(0).getHost())
            .isEqualTo(expectedRoute);
    }

    @Then("check that the {string} config map contains")
    public void checkConfigMap(String cmName, DataTable table) {
        table.asLists(String.class).forEach(row ->
            assertThat(OpenShiftUtils.getInstance().getConfigMap(cmName).getData().get(row.get(0)).contains(row.get(1).toString()))
        );
    }

    @Then("check that datavirt is deployed")
    public void checkDv() {
        OpenShiftUtils.getInstance().waiters()
            .areExactlyNPodsReady(1, "syndesis.io/component", "syndesis-dv")
            .interval(TimeUnit.SECONDS, 20)
            .timeout(TimeUnit.MINUTES, 5)
            .waitFor();
    }

    @Then("^check that jaeger (is|is not) collecting metrics for integration \"([^\"]*)\"$")
    public void checkJaeger(String shouldCollect, String integrationName) {
        TestUtils.sleepIgnoreInterrupt(30000L);
        LocalPortForward lpf = TestUtils.createLocalPortForward(
            OpenShiftUtils.getPod(p -> p.getMetadata().getName().startsWith("syndesis-jaeger")), 16686, 16686);
        final String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        JSONArray jsonData = new JSONObject(HttpUtils.doGetRequest("http://localhost:16686/api/traces?service=" + integrationId).getBody())
            .getJSONArray("data");
        TestUtils.terminateLocalPortForward(lpf);
        if ("is".equals(shouldCollect)) {
            assertThat(jsonData).size().isNotZero();
        } else {
            assertThat(jsonData).size().isZero();
        }
    }

    @Then("check correct memory limits")
    public void checkMemoryLimits() {
        final String file = "src/test/resources/operator/spec/components/resources.limits.memory.yml";
        Yaml yaml = new Yaml();
        Object data = null;
        try (FileInputStream fis = FileUtils.openInputStream(new File(file))) {
            data = yaml.load(fis);
        } catch (IOException e) {
            fail("Unable to load file " + file, e);
        }

        SoftAssertions softAssertions = new SoftAssertions();
        JSONObject components = new JSONObject((Map) data).getJSONObject("spec").getJSONObject("components");
        components.keySet().forEach(component -> {
            String memoryLimit = components.getJSONObject(component).getJSONObject("resources").getJSONObject("limits").getString("memory");
            List<DeploymentConfig> dcList = OpenShiftUtils.getInstance().deploymentConfigs()
                .withLabel("syndesis.io/component", "syndesis-" + component).list().getItems();
            softAssertions.assertThat(dcList).hasSize(1);
            softAssertions.assertThat(dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0)
                .getResources().getLimits().get("memory").getAmount())
                .as(component).isEqualTo(memoryLimit);
        });
    }

    @Then("check correct volume capacity")
    public void checkVolumeCapacity() {
        final String file = "src/test/resources/operator/spec/components/resources.volumeCapacity.yml";
        Yaml yaml = new Yaml();
        Object data = null;
        try (FileInputStream fis = FileUtils.openInputStream(new File(file))) {
            data = yaml.load(fis);
        } catch (IOException e) {
            fail("Unable to load file " + file, e);
        }

        JSONObject components = new JSONObject((Map) data).getJSONObject("spec").getJSONObject("components");
        components.keySet().forEach(component -> {
            final String pvcName = "syndesis-" + ("database".equals(component) ? "db" : component);
            final String volumeCapacity = components.getJSONObject(component).getJSONObject("resources").getString("volumeCapacity");
            assertThat(OpenShiftUtils.getInstance().persistentVolumeClaims().withName(pvcName).get()
                .getSpec().getResources().getRequests().get("storage").getAmount()).as(component).isEqualTo(volumeCapacity);
        });
    }

    @Then("check that database persistent volume capacity is greater or equals to {string}")
    public void checkDbPvCapacity(String expected) {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db") != null);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db")
                .getStatus().getPhase().equals("Bound"));
        } catch (Exception e) {
            fail("Unable to get syndesis-db pvc: ", e);
        }
        final String storageAmount = OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db")
            .getStatus().getCapacity().get("storage").getAmount();
        assertThat(storageAmount).endsWith("Gi");
        assertThat(Integer.parseInt(storageAmount.replace("Gi", "")))
            .isGreaterThanOrEqualTo(Integer.parseInt(expected.replace("Gi", "")));
    }

    @Then("check that test persistent volume is claimed by syndesis-db")
    public void checkDbPv() {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db") != null);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db")
                .getStatus().getPhase().equals("Bound"));
        } catch (Exception e) {
            fail("Unable to get syndesis-db pvc: ", e);
        }
        assertThat(OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db").getSpec().getVolumeName()).isEqualTo(TEST_PV_NAME);
    }

    @Given("create test persistent volumes with {string} storage class name")
    public void createPv(String className) {
        // Create 3 PVs, two with higher capacity that will be used by meta and prometheus, because the binding of PV is FCFS,
        // so they theoretically can steal the test-pv from db
        // These volumes won't work, but they will be available to bind
        Map<String, Quantity> capacity = new HashMap<>();
        capacity.put("storage", new Quantity("3Gi"));

        PersistentVolumeFluent.SpecNested<DoneablePersistentVolume> pv = OpenShiftUtils.getInstance().persistentVolumes()
            .createOrReplaceWithNew()
            .withNewMetadata()
            .withName(TEST_PV_NAME)
            .withLabels(TestUtils.map("operator", "test"))
            .endMetadata()
            .withNewSpec()
            .withAccessModes("ReadOnlyMany", "ReadWriteOnce", "ReadWriteMany")
            .withCapacity(capacity)
            .withNewNfs()
            .withNewServer("testServer")
            .withNewPath("/testPath")
            .endNfs();

        if (!TestUtils.isOpenshift3()) {
            pv.withStorageClassName(className);
        }
        pv.endSpec().done();

        capacity.put("storage", new Quantity("5Gi"));
        for (int i = 1; i < 3; i++) {
            pv = OpenShiftUtils.getInstance().persistentVolumes().createOrReplaceWithNew()
                .withNewMetadata()
                .withName(TEST_PV_NAME + "-" + i)
                .endMetadata()
                .withNewSpec()
                .withAccessModes("ReadWriteOnce")
                .withCapacity(capacity)
                .withNewNfs()
                .withNewServer("testServer")
                .withNewPath("/testPath")
                .endNfs();

            if (!TestUtils.isOpenshift3()) {
                // This should always be "standard" despite the actual value of className - that is used only in "test-pv" intentionally
                pv.withStorageClassName("standard");
            }
            pv.endSpec().done();
        }
    }
}
