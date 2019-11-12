package io.syndesis.qe.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
public class UpgradeSteps {
    private static final String VERSION_ENDPOINT = "/api/v1/version";
    private static final String DOCKER_HUB_SYNDESIS_TAGS_URL = "https://hub.docker.com/v2/repositories/syndesis/syndesis-server/tags/?page_size=1024";
    private static final String OPERATOR_IMAGE = "syndesis/syndesis-operator:";

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    private String integrationId;

    @When("^get upgrade versions$")
    public void getUpgradeVersions() {
        if (System.getProperty("syndesis.upgrade.version") == null) {
            // Parse "1.5"
            final BigDecimal version = new BigDecimal(Double.parseDouble(StringUtils.substring(System.getProperty("syndesis.version"), 0, 3)))
                .setScale(1, BigDecimal.ROUND_HALF_UP);
            Request request = new Request.Builder()
                .url(DOCKER_HUB_SYNDESIS_TAGS_URL)
                .build();
            String response = "";
            try {
                response = new OkHttpClient.Builder().build().newCall(request).execute().body().string();
            } catch (IOException e) {
                log.error("Unable to get version from " + VERSION_ENDPOINT);
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONObject(response).getJSONArray("results");
            List<String> tags = new ArrayList<>();
            for (Object o : jsonArray) {
                tags.add(((JSONObject) o).getString("name"));
            }

            // Grab "1.7.X" if exists, otherwise get the daily
            Optional<String> tag = tags.stream().filter(
                t -> t.matches("^" + version.toString().replaceAll("\\.", "\\\\.") + "\\.\\d+")
            ).findFirst();
            if (!tag.isPresent()) {
                tag = tags.stream().filter(
                    t -> t.matches("^" + (version + "").replaceAll("\\.", "\\\\.") + "(\\.\\d+)?-\\d{8}$")
                ).findFirst();
            }
            log.info("Setting syndesis.upgrade.version to " + tag.get());
            System.setProperty("syndesis.upgrade.version", tag.get() + "");

            // Get penultimate version - not daily
            BigDecimal previousVersion = version;
            while (previousVersion.doubleValue() >= 1.0) {
                previousVersion = previousVersion.subtract(new BigDecimal(0.1));
                BigDecimal finalPreviousVersion = previousVersion;
                Optional<String> previousTag = tags.stream().filter(
                    t -> t.matches("^" + (finalPreviousVersion.doubleValue() + "").replaceAll("\\.", "\\\\.") + "(\\.\\d+)?$")
                ).findFirst();
                if (previousTag.isPresent()) {
                    log.info("Setting syndesis.version to " + previousTag.get());
                    // Save the original syndesis version
                    System.setProperty("syndesis.upgrade.backup.version", System.getProperty("syndesis.version"));
                    System.setProperty("syndesis.version", previousTag.get());
                    break;
                }
            }
        }

        if (System.getProperty("syndesis.upgrade.old.version") != null) {
            // Allow to define daily tag using custom property, because you can't define daily version as "syndesis.version"
            // because there are no artifacts
            System.getProperty("syndesis.upgrade.backup.version", System.getProperty("syndesis.version"));
            System.setProperty("syndesis.version", System.getProperty("syndesis.upgrade.old.version"));
        }

        TestConfiguration.get().overrideSyndesisOperatorImage(OPERATOR_IMAGE + System.getProperty("syndesis.version"));
        TestConfiguration.get().overrideSyndesisCrUrl();

        log.info("Upgrade:");
        log.info("Old version: " + System.getProperty("syndesis.version"));
        log.info("New version: " + System.getProperty("syndesis.upgrade.version"));
    }

    @When("^perform syndesis upgrade to newer version using operator$")
    public void upgradeUsingOperator() {
        TestConfiguration.get().overrideSyndesisOperatorImage(OPERATOR_IMAGE + System.getProperty("syndesis.upgrade.version"));
        final List<HasMetadata> operatorResources = ResourceFactory.get(Syndesis.class).getOperatorResources();
        OpenShiftUtils.getInstance().resourceList(operatorResources).delete();
        OpenShiftUtils.getInstance().resourceList(operatorResources).createOrReplace();
    }

    @Then("^verify syndesis \"([^\"]*)\" version$")
    public void verifyVersion(String version) {
        assertThat(getSyndesisVersion()).startsWith(System.getProperty("given".equals(version) ? "syndesis.version" : "syndesis.upgrade.version"));
    }

    private String getSyndesisVersion() {
        RestUtils.reset();
        Request request = new Request.Builder()
            .url(RestUtils.getRestUrl() + VERSION_ENDPOINT)
            .header("Accept", "text/plain")
            .build();
        try {
            return new OkHttpClient.Builder().build().newCall(request).execute().body().string();
        } catch (IOException e) {
            log.error("Unable to get version from " + VERSION_ENDPOINT);
            e.printStackTrace();
        }
        return null;
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
}
