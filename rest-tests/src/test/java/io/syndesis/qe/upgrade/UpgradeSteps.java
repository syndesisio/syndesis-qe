package io.syndesis.qe.upgrade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assumptions.assumeThat;

import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.PreviousSyndesis;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpgradeSteps {
    private static final String RELEASED_OPERATOR_IMAGE = "registry.redhat.io/fuse7/fuse-online-operator";
    private static final String UPSTREAM_OPERATOR_IMAGE = "syndesis/syndesis-operator";
    private static final String DOCKER_HUB_SYNDESIS_TAGS_URL = "https://hub.docker.com/v2/repositories/syndesis/syndesis-server/tags/?page_size=1024";

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("^deploy previous Syndesis$")
    public static void deploySyndesis() {
        ResourceFactory.create(PreviousSyndesis.class);
    }

    @When("^prepare upgrade$")
    public void getUpgradeVersions() {
        Syndesis syndesis = ResourceFactory.get(PreviousSyndesis.class);
        // If it is a prod build, we can use released images from registry.redhat.io
        if (TestUtils.isProdBuild()) {
            // If it is a prod build and the version is null, it means it was started by test-runner, so skip it as for prod upgrade there is a
            // separate job
            assumeThat(TestConfiguration.upgradePreviousVersion()).isNotNull();
            final String floatingTag = TestConfiguration.syndesisOperatorImage().split(":")[1].split("-")[0];
            final BigDecimal currentVersion = new BigDecimal(floatingTag).setScale(1, BigDecimal.ROUND_HALF_UP);
            syndesis.setOperatorImage(RELEASED_OPERATOR_IMAGE + ":" + currentVersion.subtract(new BigDecimal("0.1")));
            TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_CURRENT_VERSION, TestConfiguration.syndesisVersion());
            // Previous version needs to be specified manually via system properties
        } else {
            assumeThat(TestConfiguration.syndesisInstallVersion()).as("Upgrade tests need to have "
                + TestConfiguration.SYNDESIS_INSTALL_VERSION + " property set!").isNotNull();

            // List all the tags from docker hub
            JSONArray jsonArray = new JSONObject(HttpUtils.doGetRequest(DOCKER_HUB_SYNDESIS_TAGS_URL).getBody()).getJSONArray("results");
            List<String> tags = new ArrayList<>();
            jsonArray.forEach(tag -> tags.add(((JSONObject) tag).getString("name")));

            // Get penultimate version - not daily
            BigDecimal previousVersion = new BigDecimal(TestConfiguration.syndesisInstallVersion().substring(0, 3))
                .setScale(1, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal("0.1"));
            Optional<String> previousTag = tags.stream().filter(
                t -> t.matches("^" + (previousVersion.doubleValue() + "").replaceAll("\\.", "\\\\.") + "(\\.\\d+)?$")
            ).findFirst();

            if (previousTag.isPresent()) {
                TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_PREVIOUS_VERSION, previousTag.get());
                syndesis.setOperatorImage(UPSTREAM_OPERATOR_IMAGE + ":" + previousTag.get());
            } else {
                fail("Unable to find tagged version for " + previousVersion);
            }

            TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_CURRENT_VERSION, TestConfiguration.syndesisInstallVersion());
        }

        // We want to deploy "previous" version first
        syndesis.setCrdUrl(getClass().getClassLoader().getResource("upgrade/syndesis-crd-previous.yaml").toString());
        syndesis.setCrUrl(getClass().getClassLoader().getResource("upgrade/syndesis-cr-previous.yaml").toString());

        log.info("Upgrade properties:");
        log.info("  Previous version: " + TestConfiguration.upgradePreviousVersion());
        log.info("  Current version:  " + TestConfiguration.upgradeCurrentVersion());
    }

    @When("^perform syndesis upgrade to newer version using operator$")
    public void upgradeUsingOperator() {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        syndesis.defaultValues();
        syndesis.pullOperatorImage();
        syndesis.deployCrd();
        syndesis.grantPermissions();
        syndesis.deployOperator();
    }

    @Then("^verify syndesis \"([^\"]*)\" version$")
    public void verifyVersion(String version) {
        if ("previous".equals(version)) {
            assertThat(TestUtils.getSyndesisVersion()).isEqualTo(TestConfiguration.upgradePreviousVersion());
        } else {
            if (TestConfiguration.syndesisInstallVersion() != null) {
                assertThat(TestUtils.getSyndesisVersion()).isEqualTo(TestConfiguration.syndesisInstallVersion());
            } else {
                assertThat(TestUtils.getSyndesisVersion()).isEqualTo(TestConfiguration.syndesisVersion());
            }
        }
    }

    @Then("^verify correct s2i tag for builds$")
    public void verifyImageStreams() {
        final String expected = System.getProperty("syndesis.upgrade.rollback") != null
            ? System.getProperty("syndesis.version")
            : System.getProperty("syndesis.upgrade.version");
        OpenShiftUtils.getInstance().buildConfigs().list().getItems().stream()
            .filter(bc -> bc.getMetadata().getName().startsWith("i-"))
            .forEach(bc -> assertThat(bc.getSpec().getStrategy().getSourceStrategy().getFrom().getName()).contains(expected));
    }

    @When("^delete buildconfig with name \"([^\"]*)\"$")
    public void deleteBc(String bc) {
        OpenShiftUtils.getInstance().buildConfigs().withName(bc).delete();
    }

    @Then("wait until upgrade is done")
    public void waitForUpgrade() {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs("syndesis-operator").contains("Syndesis resource upgraded"), 30000L, 600000L);
        } catch (Exception e) {
            fail("\"Syndesis resource upgraded\" wasn't found in operator log after 10 minutes");
        }
    }

    @When("rollout")
    public void rollout() {
        Component.getAllComponents().stream().filter(c -> c != Component.DB).forEach(
            component -> OpenShiftUtils.getInstance().deploymentConfigs().withName(component.getName()).deployLatest());
    }

    @Then("check that pull secret is linked in the service accounts")
    public void checkPullSecret() {
        boolean found = false;
        // If it is present in the server, it was linked to all others needed
        for (LocalObjectReference imagePullSecret : OpenShiftUtils.getInstance().getServiceAccount("syndesis-server").getImagePullSecrets()) {
            if (imagePullSecret.getName().equals(TestConfiguration.syndesisPullSecretName())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("The pull secret should be linked to service account, but wasn't").isTrue();
    }
}
