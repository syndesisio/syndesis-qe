package io.syndesis.qe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assumptions.assumeThat;

import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.PreviousSyndesis;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.http.HTTPUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.vdurmont.semver4j.Semver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpgradeSteps {
    private static final String RELEASED_OPERATOR_IMAGE = "registry.redhat.io/fuse7/fuse-online-operator";
    private static final String UPSTREAM_OPERATOR_IMAGE = "syndesis/syndesis-operator";
    private static final String DOCKER_HUB_SYNDESIS_TAGS_URL = "https://hub.docker.com/v2/repositories/syndesis/syndesis-server/tags/?page_size=100";

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("deploy previous Syndesis CR {string}")
    public void deploySyndesis(String crFile) {
        Syndesis syndesis = ResourceFactory.get(PreviousSyndesis.class);
        syndesis.setCrUrl(getClass().getClassLoader().getResource("upgrade/" + crFile).toString());
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
            String majorMinorMavenVersion = getMajorMinor(TestConfiguration.upgradePreviousVersion());
            BigDecimal majorMinorImageVersion = new BigDecimal(majorMinorMavenVersion).subtract(new BigDecimal("0.3"));
            // Parse the previous tag from maven artifacts
            syndesis.setOperatorImage(RELEASED_OPERATOR_IMAGE + ":" + majorMinorImageVersion.toPlainString());
            TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_CURRENT_VERSION, TestConfiguration.syndesisVersion());
            // Previous version needs to be specified manually via system properties
        } else {
            assumeThat(TestConfiguration.syndesisInstallVersion()).as("Upgrade tests need to have "
                + TestConfiguration.SYNDESIS_INSTALL_VERSION + " property set!").isNotNull();

            // List all the tags from docker hub
            String next = DOCKER_HUB_SYNDESIS_TAGS_URL;
            List<String> tags = new ArrayList<>();
            while (next != null) {
                JSONObject response = new JSONObject(HTTPUtils.doGetRequest(next).getBody());
                response.getJSONArray("results").forEach(tag -> tags.add(((JSONObject) tag).getString("name")));
                try {
                    next = response.getString("next");
                } catch (JSONException ex) {
                    next = null;
                }
            }
            Collections.sort(tags);

            String previousTag = getPreviousVersion(getMajorMinor(TestConfiguration.syndesisInstallVersion()), tags);
            if (!previousTag.isEmpty()) {
                TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_PREVIOUS_VERSION, previousTag);
                syndesis.setOperatorImage(UPSTREAM_OPERATOR_IMAGE + ":" + previousTag);
            } else {
                fail("Unable to find previous version for " + TestConfiguration.syndesisInstallVersion());
            }

            TestConfiguration.get().overrideProperty(TestConfiguration.SYNDESIS_UPGRADE_CURRENT_VERSION, TestConfiguration.syndesisInstallVersion());
        }

        // We want to deploy "previous" version first
        syndesis.setCrdUrl(getClass().getClassLoader().getResource("upgrade/syndesis-crd-previous.yaml").toString());

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
            OpenShiftWaitUtils.waitFor(() -> {
                JSONObject cr = new JSONObject(ResourceFactory.get(Syndesis.class).getCr());
                return !"Installed".equals(cr.getJSONObject("status").getString("phase"));
            });
        } catch (TimeoutException | InterruptedException e) {
            InfraFail.fail("Timeout waiting for CR status to be changed from \"Installed\"");
        }

        try {
            OpenShiftWaitUtils.waitFor(() -> {
                JSONObject cr = new JSONObject(ResourceFactory.get(Syndesis.class).getCr());
                return "Installed".equals(cr.getJSONObject("status").getString("phase"));
            }, 15 * 60000L);
        } catch (TimeoutException | InterruptedException e) {
            InfraFail.fail("Timeout waiting for CR status to be \"Installed\"");
        }
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

    @Then("verify upgrade integration {string}")
    public void checkIntegration(String name) {
        String[] lines = OpenShiftUtils.getIntegrationLogs(name).split("\n");
        final String lastLine = lines[lines.length - 1];
        TestUtils.sleepIgnoreInterrupt(10000L);
        String logsAfter = OpenShiftUtils.getIntegrationLogs(name);
        assertThat(logsAfter.substring(logsAfter.indexOf(lastLine))).contains("[[options]]");
    }

    private String getPreviousVersion(String current, List<String> tags) {
        // Semver needs 1.2.3 version style, so add ".0" if it's missing
        if (current.matches("^\\d\\.\\d+")) {
            current += ".0";
        }
        Semver currentVersion = new Semver(current);
        // Find previous version by incrementing until the next one is equal to current
        Semver increment = new Semver("1.0.0");
        String previousVersion = "";
        // Max minor version in one version
        int maxMinor = 20;
        while (!currentVersion.equals(increment)) {
            // For lambda to be final
            Semver finalIncrement = increment;
            // Check if this tag exists, an if yes, use the "latest" as the version
            String previousTag = tags.stream()
                .filter(t -> t.matches("^" + getMajorMinor(finalIncrement.toString()).replaceAll("\\.", "\\\\.") + "(\\.\\d+)?$")
            ).reduce((first, second) -> second).orElse(null);

            // If this tag exists, save it
            if (previousTag != null) {
                previousVersion = previousTag;
            }

            increment = increment.getMinor() == maxMinor ? increment.nextMajor() : increment.withIncMinor();
        }
        log.info("Previous version for {} is {}", current, previousVersion);
        return previousVersion;
    }

    private String getMajorMinor(String version) {
        final String expr = "^(\\d\\.\\d+)";
        if (version.matches(expr)) {
            return version;
        } else {
            Matcher matcher = Pattern.compile(expr).matcher(version);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                fail("Unable to parse major.minor version from " + version);
            }
        }
        // This won't happen
        return "";
    }
}
