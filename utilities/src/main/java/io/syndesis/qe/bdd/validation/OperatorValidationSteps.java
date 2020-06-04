package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.Addon;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ExtensionsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.ExternalDatabase;
import io.syndesis.qe.resource.impl.Jaeger;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.AccountUtils;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.S3Utils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeFluent;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperatorValidationSteps {
    public static final String TEST_PV_NAME = "test-pv";

    private static final String SYNDESIS_BACKUP_BUCKET_PREFIX = "syndesis-backup";
    private static final String SYNDESIS_BACKUP_SECRET_NAME = "syndesis-backup-s3";

    private Path backupTempDir;
    private Path backupTempFile;

    @Autowired
    @Lazy
    private IntegrationsEndpoint integrations;

    @Autowired
    @Lazy
    private ConnectionsEndpoint connections;

    @Autowired
    @Lazy
    private ExtensionsEndpoint extensions;

    @Autowired
    @Lazy
    private S3Utils s3;

    @Given("^deploy Syndesis CRD$")
    public void deployCRD() {
        ResourceFactory.get(Syndesis.class).deployCrd();
    }

    @Given("^install cluster resources$")
    public void installCluster() {
        ResourceFactory.get(Syndesis.class).installCluster();
    }

    @Given("^grant permissions to user$")
    public void grantPermissions() {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        syndesis.pullOperatorImage();
        syndesis.grantPermissions();
    }

    @Given("^create pull secret$")
    public void createPullSecret() {
        ResourceFactory.get(Syndesis.class).createPullSecret();
    }

    @Given("^deploy Syndesis operator$")
    public void deployOperator() {
        ResourceFactory.get(Syndesis.class).deployOperator();
    }

    @When("^deploy Syndesis CR from file \"([^\"]*)\"")
    public void deployCrFromFile(String file) {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/operator/" + file), "UTF-8");
            if (content.contains("REPLACE_REPO")) {
                content = content.replace("REPLACE_REPO", TestUtils.isProdBuild() ? TestConfiguration.prodRepository()
                    : TestConfiguration.upstreamRepository());
            }
            if (content.contains("REPLACE_QUERY_URL")) {
                content = content.replace("REPLACE_QUERY_URL", ResourceFactory.get(Jaeger.class).getQueryServiceHost());
            }
            if (content.contains("REPLACE_COLLECTOR_URL")) {
                content = content.replace("REPLACE_COLLECTOR_URL", ResourceFactory.get(Jaeger.class).getCollectorServiceHost());
            }
            syndesis.getSyndesisCrClient().create(TestConfiguration.openShiftNamespace(), content);
            //don't do workarounds for external Jaeger
            if (syndesis.isAddonEnabled(Addon.JAEGER) && !syndesis.containsAddonProperty(Addon.JAEGER, "collectorUri")) {
                syndesis.jaegerWorkarounds();
            }
        } catch (IOException e) {
            fail("Unable to open file " + file, e);
        }
    }

    @Then("^check deployed syndesis version$")
    public void checkVersion() {
        final String deployedVersion = TestUtils.getSyndesisVersion();
        if (TestConfiguration.syndesisInstallVersion() != null) {
            // If the install version is "1.X", the deployed version can be "1.X.Y"
            if (TestConfiguration.syndesisInstallVersion().matches("^\\d\\.\\d$")) {
                assertThat(deployedVersion).matches("^(\\d\\.){2}\\d+$");
                assertThat(deployedVersion).startsWith(TestConfiguration.syndesisInstallVersion());
            } else {
                assertThat(deployedVersion).isEqualTo(TestConfiguration.syndesisInstallVersion());
            }
        } else if (TestConfiguration.syndesisNightlyVersion() != null) {
            assertThat(deployedVersion).isEqualTo(TestConfiguration.syndesisNightlyVersion());
        } else {
            assertThat(deployedVersion).isEqualTo(TestConfiguration.syndesisVersion());
        }
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
        } catch (TimeoutException | InterruptedException | IOException e) {
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

    @Then("^check that jaeger pod \"([^\"]*)\" (is|is not) collecting metrics for integration \"([^\"]*)\"$")
    public void checkJaeger(String jaegerPodName, String shouldCollect, String integrationName) {
        TestUtils.sleepIgnoreInterrupt(30000L);
        LocalPortForward lpf = TestUtils.createLocalPortForward(
            OpenShiftUtils.getPod(p -> p.getMetadata().getName().startsWith(jaegerPodName)), 16686, 16686);
        final String integrationId = integrations.getIntegrationId(integrationName).get();
        String host = "localhost:16686"; //host for default syndesis-jaeger
        if (ResourceFactory.get(Syndesis.class).containsAddonProperty(Addon.JAEGER, "collectorUri")) {
            host = ResourceFactory.get(Jaeger.class).getQueryServiceHost();
        }
        JSONArray jsonData = new JSONObject(HttpUtils.doGetRequest(
            "http://" + host + "/api/traces?service=" + integrationId)
            .getBody())
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
            String expectedLimit = components.getJSONObject(component).getJSONObject("resources").getString("memory");
            List<DeploymentConfig> dcList = OpenShiftUtils.getInstance().deploymentConfigs()
                .withLabel("syndesis.io/component", "syndesis-" + ("database".equals(component) ? "db" : component)).list().getItems();
            softAssertions.assertThat(dcList).hasSize(1);
            final Quantity currentLimit = dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0)
                .getResources().getLimits().get("memory");
            softAssertions.assertThat(currentLimit.getAmount() + currentLimit.getFormat())
                .as(component).isEqualTo(expectedLimit);
        });
        softAssertions.assertAll();
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
            final String expectedCapacity = components.getJSONObject(component).getJSONObject("resources").getString("volumeCapacity");
            final Quantity currentCapacity = OpenShiftUtils.getInstance().persistentVolumeClaims().withName(pvcName).get()
                .getSpec().getResources().getRequests().get("storage");
            assertThat(currentCapacity.getAmount() + currentCapacity.getFormat()).as(component).isEqualTo(expectedCapacity);
        });
    }

    @Then("check that database persistent volume capacity is greater or equals to {string}")
    public void checkDbPvCapacity(String expected) {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db") != null);
            OpenShiftWaitUtils.waitFor(() -> !OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db")
                .getSpec().getVolumeName().isEmpty());
        } catch (TimeoutException | InterruptedException e) {
            fail("Unable to get syndesis-db pvc: ", e);
        }

        final Quantity capacity = OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db").getStatus().getCapacity().get("storage");
        assertThat(capacity.getFormat()).isEqualTo("Gi");
        assertThat(Integer.parseInt(capacity.getAmount()))
            .isGreaterThanOrEqualTo(Integer.parseInt(expected.replaceAll("[a-zA-Z]", "")));
    }

    @Then("check that test persistent volume is claimed by syndesis-db")
    public void checkDbPv() {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db") != null);
            OpenShiftWaitUtils.waitFor(() -> !OpenShiftUtils.getInstance().getPersistentVolumeClaim("syndesis-db")
                .getSpec().getVolumeName().isEmpty());
        } catch (TimeoutException | InterruptedException e) {
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
        if (!"".equals(className) && OpenShiftUtils.getInstance().storage().storageClasses().withName(className).get() == null) {
            log.info("Creating storage class " + className);
            OpenShiftUtils.getInstance().storage().storageClasses().createOrReplaceWithNew()
                .withNewMetadata().withName(className).endMetadata()
                .withProvisioner("kubernetes.io/cinder")
                .done();
        }
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

        if (!className.isEmpty()) {
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
                // This should always be the default value despite the actual value of className - that is used only in "test-pv" intentionally
                pv.withStorageClassName("standard");
            }
            pv.endSpec().done();
        }
    }

    @When("create pull secret for backup")
    public void createPullSecretForBackup() {
        Account aws = AccountUtils.get(Account.Name.AWS);
        OpenShiftUtils.getInstance().createSecret(
            new SecretBuilder()
                .withNewMetadata()
                .withName(SYNDESIS_BACKUP_SECRET_NAME)
                .endMetadata()
                .withStringData(TestUtils.map(
                    "secret-key-id", aws.getProperty("accessKey"),
                    "secret-access-key", aws.getProperty("secretKey"),
                    "region", aws.getProperty("region").toLowerCase().replaceAll("_", "-"),
                    "bucket-name", S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX)
                ))
                .build()
        );
    }

    @Then("wait for backup")
    public void waitForBackup() {
        log.info("Waiting until the operator does the backup...");
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs("syndesis-operator").contains("backup for syndesis done"),
                30000L, 10 * 60000L);
            log.info("Backup done");
        } catch (TimeoutException | InterruptedException e) {
            fail("Exception thrown while waiting for backup log", e);
        }
    }

    @When("download the backup file")
    public void downloadBackup() {
        final String prefix = OpenShiftUtils.getInstance().getImageStream("syndesis-operator")
            .getSpec().getTags().get(0).getName().split("-")[0];
        final String fullFileName = s3.getFileNameWithPrefix(S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX), prefix);
        try {
            backupTempFile = Files.createTempFile("syndesis", "backup");
        } catch (IOException e) {
            fail("Unable to create local backup file: ", e);
        }
        log.debug("Downloading backup file from S3 bucket " + S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX)
            + "to: " + backupTempFile.toString());
        s3.downloadFile(S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX), fullFileName, backupTempFile);
    }

    @When("prepare backup folder")
    public void prepareBackupFolder() {
        try {
            // There is some mess with access rights in docker when using createTempDirectory, so create temp directory manually
            backupTempDir = Files.createDirectory(Paths.get("/tmp", "syndesis-backup-" + new Random(new Date().getTime()).nextInt()));
            backupTempDir.toFile().setReadable(true, false);
            log.info("Created backup dir: " + backupTempDir.toString());
        } catch (IOException e) {
            fail("Unable to create local backup folder: ", e);
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(backupTempFile.toFile());
        } catch (IOException e) {
            fail("Unable to open zip file: ", e);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(backupTempDir.toFile(), entry.getName());
            if (entry.isDirectory()) {
                entryDestination.mkdirs();
            } else {
                entryDestination.getParentFile().mkdirs();
                try (InputStream in = zipFile.getInputStream(entry)) {
                    try (OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    }
                } catch (IOException e) {
                    fail("Unable to unzip backup file: ", e);
                }
            }
        }
    }

    @When("perform {string} {string} restore from backup")
    public void performRestore(String method, String type) {
        if ("manual".equals(method)) {
            performManualRestore(type);
        } else {
            performOperatorRestore();
        }
    }

    public void performOperatorRestore() {
        try {
            new ProcessBuilder("docker",
                "run",
                "--rm",
                "-v",
                OpenShiftUtils.binary().getOcConfigPath() + ":/tmp/kube/config:z",
                "-v",
                backupTempDir.toAbsolutePath().toString() + ":/tmp/syndesis-backup:z",
                "--entrypoint",
                "syndesis-operator",
                TestConfiguration.syndesisOperatorImage(),
                "restore",
                "--backup",
                "/tmp/syndesis-backup",
                "--namespace",
                TestConfiguration.openShiftNamespace(),
                "--config",
                "/tmp/kube/config"
            ).start().waitFor();
        } catch (InterruptedException | IOException e) {
            fail("Unable to invoke restore using operator", e);
        }
    }

    public void performManualRestore(String type) {
        final String dbName;
        final String containerName;
        Pod dbPod;
        if ("standard".equals(type)) {
            dbPod = OpenShiftUtils.getAnyPod("syndesis.io/component", "syndesis-db").get();
            dbName = "syndesis";
            containerName = "postgresql";
        } else {
            dbPod = OpenShiftUtils.getPodByPartialName("custom-postgres").get();
            dbName = "testdb";
            containerName = null;
        }

        // Copy the dump into the db pod
        OpenShiftUtils.binary().execute(
            "cp",
            backupTempDir.toAbsolutePath() + "/syndesis-db.dump",
            dbPod.getMetadata().getName() + ":/tmp/syndesis-db.dump");

        // Also copy the manual restore script
        OpenShiftUtils.binary().execute(
            "cp",
            new File("src/test/resources/operator/manual_restore.sh").getAbsolutePath(),
            dbPod.getMetadata().getName() + ":/tmp/manual_restore.sh");

        // Invoke manual restore script
        log.debug(OpenShiftUtils.getInstance().podShell(dbPod, containerName)
            .executeWithBash("/tmp/manual_restore.sh " + dbName).getOutput());
    }

    @Then("check that connection {string} exists")
    public void checkConnection(String connection) {
        assertThat(connections.getConnectionByName(connection)).isNotNull();
    }

    @Then("check that connection {string} doesn't exist")
    public void checkConnectionDoesNotExist(String connection) {
        assertThat(connections.getConnectionByName(connection)).isNull();
    }

    @Then("check that extension {string} exists")
    public void checkExtension(String extension) {
        // This fails when it is not present, so we don't need the value
        extensions.getExtensionByName(extension);
    }

    @When("redeploy custom database")
    public void redeployDb() {
        ResourceFactory.destroy(ExternalDatabase.class);
        ResourceFactory.create(ExternalDatabase.class);
    }

    @When("clean backup S3 bucket")
    public void cleanS3() {
        s3.cleanS3Bucket(S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX));
    }

    @Then("verify that there are {int} backups in S3")
    public void verifyBackups(int count) {
        assertThat(s3.getFileCount(S3BucketNameBuilder.getBucketName(SYNDESIS_BACKUP_BUCKET_PREFIX))).isEqualTo(count);
    }
}
