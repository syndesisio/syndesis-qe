package io.syndesis.qe.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.assertj.core.api.SoftAssertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
public class UpgradeSteps {
    private static final String SYNDESIS = Paths.get("..", "..", "syndesis").toAbsolutePath().toString();
    private static final String UPGRADE_FOLDER = Paths.get(SYNDESIS, "tools", "upgrade").toFile().toString();
    private static final String UPGRADE_TEMPLATE = Paths.get(SYNDESIS, "install", "syndesis.yml").toString();
    private static final String VERSION_ENDPOINT = "/api/v1/version";
    private static final String DOCKER_HUB_SYNDESIS_TAGS_URL = "https://hub.docker.com/v2/repositories/syndesis/syndesis-server/tags/";
    private static final String BACKUP_DIR = "/tmp/backup";

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    private String integrationId;

    @When("^get upgrade versions$")
    public void getUpgradeVersions() {
        if (System.getProperty("syndesis.upgrade.version") == null) {
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

            // Use only daily tags
            Pattern pattern = Pattern.compile("^\\d\\.\\d(\\.\\d+)?-\\d{8}$");

            for (String tag : tags) {
                Matcher matcher = pattern.matcher(tag);
                if (matcher.matches()) {
                    if (System.getProperty("syndesis.upgrade.version") == null) {
                        log.info("Setting syndesis.upgrade.version to " + tag);
                        System.setProperty("syndesis.upgrade.version", tag);
                    } else {
                        log.info("Setting syndesis.version to " + tag);
                        System.setProperty("syndesis.version", tag);
                        break;
                    }
                }
            }

            TestConfiguration.get().overrideSyndesisVersion(System.getProperty("syndesis.version"));
        }
    }

    @When("^perform syndesis upgrade to newer version$")
    public void syndesisUpgrade() {
        ProcessBuilder pb = new ProcessBuilder(Paths.get(UPGRADE_FOLDER, "upgrade.sh").toString(),
                "--template ", UPGRADE_TEMPLATE,
                "--backup", BACKUP_DIR,
                "--migration", Paths.get(UPGRADE_FOLDER, "migration").toString());
        pb.directory(new File(UPGRADE_FOLDER));

        try {
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
        } catch (Exception e) {
            log.error("Error while running upgrade script: ", e);
            e.printStackTrace();
        }
    }

    @When("^perform syndesis upgrade to newer version using operator$")
    public void upgradeUsingOperator() {
        OpenShiftUtils.client().imageStreams().withName("syndesis-operator").edit()
                .editSpec()
                    .editFirstTag()
                        .withName(System.getProperty("syndesis.version"))
                        .editFrom()
                            .withName("docker.io/syndesis/syndesis-operator:" + System.getProperty("syndesis.upgrade.version"))
                        .endFrom()
                    .endTag()
                .endSpec()
                .done();
    }

    @Then("^verify syndesis \"([^\"]*)\" version$")
    public void verifyVersion(String version) {
        assertThat(getSyndesisVersion()).isEqualTo(System.getProperty("given".equals(version) ? "syndesis.version" : "syndesis.upgrade.version"));
    }

    @When("^perform test modifications$")
    public void performTestModifications() {
        modifyTemplate();
        modifyDbScripts();
        modifyUpgradeDbScript();
        copyStatefulScript();
        getSyndesisCli();
    }

    private void modifyTemplate() {
        // Change the install template to use newer version
        String template;
        try {
            template = FileUtils.readFileToString(new File(UPGRADE_TEMPLATE), "UTF-8");
            String version = StringUtils.substringBefore(StringUtils.substringAfter(template, "syndesis: ").substring(1), "\"");
            template = template.replaceAll(version, System.getProperty("syndesis.upgrade.version"));

            // Modify deployment config
            // This is easier than messing with yaml directly and it adds the env for syndesis-meta and syndesis-server
            if (!template.contains("- name: TEST")) {
                template = StringUtils.replaceAll(template, "tmp", "tmp\"\n          - name: TEST\n            value: \"UPGRADE");
            }
        FileUtils.write(new File(UPGRADE_TEMPLATE), template, "UTF-8", false);
        } catch (IOException e) {
            log.error("Unable to modify template", e);
        }
    }

    private void modifyDbScripts() {
        integrationId = integrationsEndpoint.getIntegrationId("upgrade").get();
        String upgradeResourcesPath = new File("src/test/resources/upgrade").getAbsolutePath();
        // Replace placeholder in upgrade scripts
        try {
            File destination = Paths.get(upgradeResourcesPath, "up-98.js").toFile();
            destination.delete();
            String content = FileUtils.readFileToString(Paths.get(upgradeResourcesPath, "up-98-template.js").toFile(), "UTF-8");
            FileUtils.write(destination, content.replaceAll("INTEGRATION_ID", integrationId), "UTF-8", false);

            destination = Paths.get(upgradeResourcesPath, "up-99.js").toFile();
            destination.delete();
            content = FileUtils.readFileToString(Paths.get(upgradeResourcesPath, "up-99-template.js").toFile(), "UTF-8");
            FileUtils.write(destination, content.replaceAll("INTEGRATION_ID", integrationId), "UTF-8", false);
        } catch (IOException e) {
            log.error("Unable to modify db scripts", e);
        }
    }

    private void modifyUpgradeDbScript() {
        String upgradeResourcesPath = new File("src/test/resources/upgrade").toURI().toString();
        // Make the syndesis-cli migrate to newest version and use scripts from resources
        String upgradeDb;
        try {
            upgradeDb = FileUtils.readFileToString(Paths.get(UPGRADE_FOLDER, "steps", "upgrade_10_migrate_db").toFile(), "UTF-8");
            if (!upgradeDb.contains("-t 99")) {
                upgradeDb = upgradeDb.replaceAll("syndesis-cli.jar migrate", "syndesis-cli.jar migrate -t 99 -f "
                        + upgradeResourcesPath);

                upgradeDb = upgradeDb.replaceAll("port=5432", "port=5433");
                upgradeDb = upgradeDb.replaceAll("pod 5432", "pod 5433\\:5432");
                FileUtils.write(Paths.get(UPGRADE_FOLDER, "steps", "upgrade_10_migrate_db").toFile(), upgradeDb, "UTF-8", false);
            }
        } catch (IOException e) {
            log.error("Unable to modify modify cli", e);
        }

    }

    private void copyStatefulScript() {
        // Move the config change script to resource folder
        try {
            FileUtils.copyFile(new File("src/test/resources/upgrade/99-change-ui-config.sh"),
                Paths.get(UPGRADE_FOLDER, "migration", "resource",
                    System.getProperty("syndesis.upgrade.version"), "99-change-ui-config.sh").toFile());
        } catch (IOException e) {
            log.error("Unable to modify copy script", e);
        }
    }

    @Then("^verify successful test modifications$")
    public void verifySuccessfulTestModifications() {
        verifyTestModifications(false);
    }

    @Then("^verify test modifications rollback")
    public void verifyTestModificationsRollback() {
        verifyTestModifications(true);
    }

    @When("^add rollback cause to upgrade script")
    public void addRollbackCause() {
        // Ideally this should be done in upgrade_60_restart_all but there is no rollback for that at the moment
        try {
            File scriptFile = Paths.get(UPGRADE_FOLDER, "steps", "upgrade_50_replace_template").toFile();
            String script = FileUtils.readFileToString(scriptFile, "UTF-8");

            FileUtils.write(scriptFile, StringUtils.replaceAll(script, "update_version \\$tag", "update_version \\$tag; exit 1"), "UTF-8");
        } catch (IOException e) {
            log.error("Unable to manipulate file for rollback", e);
        }
    }

    @Then("^wait until upgrade pod is finished$")
    public void waitForUpgrade() {
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName("syndesis-upgrade");
        int retries = 0;
        while (!pod.isPresent() && retries < 30) {
            TestUtils.sleepIgnoreInterrupt(5000L);
            retries++;
            pod = OpenShiftUtils.getPodByPartialName("syndesis-upgrade");
        }

        retries = 0;
        log.info("Waiting for syndesis-upgrade pod to complete");
        // 10 minutes
        while (!"Completed".equals(pod.get().getStatus().getPhase()) && retries < 120) {
            TestUtils.sleepIgnoreInterrupt(5000L);
            retries++;
        }
    }

    private void verifyTestModifications(boolean rollback) {
        // ConfigMap label change
        ConfigMap cm = OpenShiftUtils.client().configMaps().withName("syndesis-ui-config").get();

        // New ENV variable in syndesis-server and syndesis-meta
        EnvVar dcEnvVar = null;
        DeploymentConfig dc = OpenShiftUtils.client().deploymentConfigs().withName("syndesis-server").get();
        for (EnvVar envVar : dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()) {
            if (envVar.getName().equals("TEST")) {
                dcEnvVar = envVar;
                break;
            }
        }

        final EnvVar finalDcEnvVar = dcEnvVar;

        if (rollback) {
            SoftAssertions.assertSoftly(softAssertions -> {
                softAssertions.assertThat(cm.getMetadata().getLabels().get("TEST")).isNull();
                softAssertions.assertThat(finalDcEnvVar).isNull();
                softAssertions.assertThat(integrationsEndpoint.get(integrationId).getName()).isEqualTo("upgrade");
                softAssertions.assertThat(integrationsEndpoint.get(integrationId).getDescription().get()).isEqualTo("Awkward integration.");
            });
        } else {
            SoftAssertions.assertSoftly(softAssertions -> {
                softAssertions.assertThat(cm.getMetadata().getLabels().get("TEST")).isEqualTo("UPGRADE");
                softAssertions.assertThat(finalDcEnvVar.getValue()).isEqualTo("UPGRADE");
                softAssertions.assertThat(integrationsEndpoint.get(integrationId).getName()).isEqualTo("UPGRADE INTEGRATION NAME");
                softAssertions.assertThat(integrationsEndpoint.get(integrationId).getDescription().get()).isEqualTo("UPGRADE INTEGRATION DESCRIPTION");
            });
        }

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

    private void getSyndesisCli() {
        if (!Paths.get(UPGRADE_FOLDER, "syndesis-cli.jar").toFile().exists()) {
            log.info("Expecting to be run on jenkins, trying to copy ../../syndesis/app/server/cli/target/syndesis-cli.jar");
            try {
                FileUtils.copyFile(Paths.get("../../syndesis/app/server/cli/target/syndesis-cli.jar").toFile(), Paths.get(UPGRADE_FOLDER, "syndesis-cli.jar").toFile());
            } catch (IOException e) {
                log.error("Unable to copy syndesis-cli.jar");
            }
        }
    }

    @And("^clean upgrade modifications$")
    public void cleanUpgradeModifications() {
        log.info("Running \"git checkout .\" in \"" + SYNDESIS + "\"");
        ProcessBuilder pb = new ProcessBuilder("git", "checkout", ".");
        pb.directory(new File(SYNDESIS));

        try {
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
        } catch (Exception e) {
            log.error("Error while running script: ", e);
            e.printStackTrace();
        }
    }
}
