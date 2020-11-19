package io.syndesis.qe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.addon.Addon;
import io.syndesis.qe.component.Component;
import io.syndesis.qe.component.ComponentUtils;
import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.ExternalDatabase;
import io.syndesis.qe.resource.impl.Jaeger;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.aws.S3BucketNameBuilder;
import io.syndesis.qe.utils.aws.S3Utils;
import io.syndesis.qe.utils.http.HTTPUtils;
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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolume;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAffinity;
import io.fabric8.kubernetes.api.model.NodeSelector;
import io.fabric8.kubernetes.api.model.NodeSelectorTerm;
import io.fabric8.kubernetes.api.model.PersistentVolumeFluent;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Toleration;
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
    private S3Utils s3;

    @Given("^deploy Syndesis CRD$")
    public void deployCRD() {
        ResourceFactory.get(Syndesis.class).installCluster();
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

    @When("^deploy Syndesis CR( from file \"([^\"]*)\")?")
    public void deployCrFromFile(String file) {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        try {
            if (file == null) {
                syndesis.deploySyndesisViaOperator();
                //don't do workarounds for external Jaeger
                if (syndesis.isAddonEnabled(Addon.JAEGER) && !syndesis.containsAddonProperty(Addon.JAEGER, "collectorUri")) {
                    syndesis.jaegerWorkarounds();
                }
            } else {
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
                if (content.contains("REPLACE_NODE")) {
                    Optional<Node> worker = OpenShiftUtils.getInstance().nodes().list().getItems().stream()
                        .filter(n -> n.getMetadata().getName().contains("worker") && n.getSpec().getTaints().isEmpty()).findFirst();
                    if (!worker.isPresent()) {
                        fail("There are no worker nodes with empty taints!");
                    }
                    content = content.replace("REPLACE_NODE", worker.get().getMetadata().getName());
                }
                syndesis.getSyndesisCrClient().create(TestConfiguration.openShiftNamespace(), content);
                //don't do workarounds for external Jaeger
                if (syndesis.isAddonEnabled(Addon.JAEGER) && !syndesis.containsAddonProperty(Addon.JAEGER, "collectorUri")) {
                    syndesis.jaegerWorkarounds();
                }
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

    @Then("^check that the \"([^\"]*)\" config map (contains|doesn't contain)$")
    public void checkConfigMap(String cmName, String shouldContain, DataTable table) {
        SoftAssertions asserts = new SoftAssertions();
        if ("contains".equals(shouldContain)) {
            table.asLists(String.class).forEach(row ->
                asserts.assertThat(OpenShiftUtils.getInstance().getConfigMap(cmName).getData().get(row.get(0))).contains(row.get(1).toString())
            );
        } else {
            table.asLists(String.class).forEach(row ->
                asserts.assertThat(OpenShiftUtils.getInstance().getConfigMap(cmName).getData().get(row.get(0))).doesNotContain(row.get(1).toString())
            );
        }
        asserts.assertAll();
    }

    @Then("^check that jaeger pod \"([^\"]*)\" (is|is not) collecting metrics for integration \"([^\"]*)\"$")
    public void checkJaeger(String jaegerPodName, String shouldCollect, String integrationName) {
        TestUtils.sleepIgnoreInterrupt(30000L);
        LocalPortForward lpf = OpenShiftUtils.createLocalPortForward(
            OpenShiftUtils.getPod(p -> p.getMetadata().getName().startsWith(jaegerPodName)), 16686, 16686);
        final String integrationId = integrations.getIntegrationId(integrationName).get();
        String host = "localhost:16686"; //host for default syndesis-jaeger
        if (ResourceFactory.get(Syndesis.class).containsAddonProperty(Addon.JAEGER, "collectorUri")) {
            host = ResourceFactory.get(Jaeger.class).getQueryServiceHost();
        }
        JSONArray jsonData = new JSONObject(HTTPUtils.doGetRequest(
            "http://" + host + "/api/traces?service=" + integrationId)
            .getBody())
            .getJSONArray("data");
        OpenShiftUtils.terminateLocalPortForward(lpf);
        if ("is".equals(shouldCollect)) {
            assertThat(jsonData).size().isNotZero();
        } else {
            assertThat(jsonData).size().isZero();
        }
    }

    @Then("check correct memory limits")
    public void checkMemoryLimits() {
        final String file = "src/test/resources/operator/spec/components/resources.limits.requests.memory.cpu.yml";
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
            String expectedMemoryLimit = components.getJSONObject(component).getJSONObject("resources").getJSONObject("limit").getString("memory");
            String expectedCpuLimit = components.getJSONObject(component).getJSONObject("resources").getJSONObject("limit").getString("cpu");
            String expectedMemoryRequests = components.getJSONObject(component).getJSONObject("resources").getJSONObject("request").getString("memory");
            String expectedCpuRequests = components.getJSONObject(component).getJSONObject("resources").getJSONObject("request").getString("cpu");
            List<DeploymentConfig> dcList = OpenShiftUtils.getInstance().deploymentConfigs()
                .withLabel("syndesis.io/component", "syndesis-" + ("database".equals(component) ? "db" : component)).list().getItems();
            softAssertions.assertThat(dcList).hasSize(1);
            final Quantity currentMemoryLimit = dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits().get("memory");
            softAssertions.assertThat(currentMemoryLimit).as(component + " memory limit is null").isNotNull();
            if (currentMemoryLimit != null) {
                softAssertions.assertThat(currentMemoryLimit.getAmount() + currentMemoryLimit.getFormat())
                    .as(component + " memory limit").isEqualTo(expectedMemoryLimit);
            }
            final Quantity currentCpuLimit = dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getLimits().get("cpu");
            softAssertions.assertThat(currentCpuLimit).as(component + " cpu limit is null").isNotNull();
            if (currentCpuLimit != null) {
                softAssertions.assertThat(currentCpuLimit.getAmount() + currentCpuLimit.getFormat())
                    .as(component + " cpu limit").isEqualTo(expectedCpuLimit);
            }
            final Quantity currentMemoryRequests = dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("memory");
            softAssertions.assertThat(currentMemoryRequests).as(component + " memory requests is null").isNotNull();
            if (currentMemoryRequests != null) {
                softAssertions.assertThat(currentMemoryRequests.getAmount() + currentMemoryRequests.getFormat())
                    .as(component + " memory requests").isEqualTo(expectedMemoryRequests);
            }
            final Quantity currentCpuRequests = dcList.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).getResources().getRequests().get("cpu");
            softAssertions.assertThat(currentCpuRequests).as(component + " cpu requests is null").isNotNull();
            if (currentCpuRequests != null) {
                softAssertions.assertThat(currentCpuRequests.getAmount() + currentCpuRequests.getFormat())
                    .as(component + " cpu requests").isEqualTo(expectedCpuRequests);
            }
        });
        softAssertions.assertAll();
    }

    @Then("check that {string} persistent volume capacity is greater or equals to {string}")
    public void checkDbPvCapacity(String pvc, String expected) {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc) != null);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc)
                .getStatus().getPhase().equals("Bound"));
        } catch (TimeoutException | InterruptedException e) {
            fail("Unable to get " + pvc + " pvc: ", e);
        }

        final Quantity capacity = OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc).getStatus().getCapacity().get("storage");
        assertThat(capacity.getFormat()).isEqualTo("Gi");
        assertThat(Integer.parseInt(capacity.getAmount()))
            .isGreaterThanOrEqualTo(Integer.parseInt(expected.replaceAll("[a-zA-Z]", "")));
    }

    @Then("check that test persistent volume is claimed by {string}")
    public void checkDbPv(String pvc) {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc) != null);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc)
                .getStatus().getPhase().equals("Bound"));
        } catch (TimeoutException | InterruptedException e) {
            fail("Unable to get syndesis-db pvc: ", e);
        }
        assertThat(OpenShiftUtils.getInstance().getPersistentVolumeClaim(pvc).getSpec().getVolumeName()).isEqualTo(TEST_PV_NAME);
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

        // The default storage class for OCP3 is empty, for OCP4 is "standard", so if the className is empty, we should use the default one
        if ("".equals(className)) {
            if (!OpenShiftUtils.isOpenshift3()) {
                pv.withStorageClassName("standard");
            }
        } else {
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

            if (!OpenShiftUtils.isOpenshift3()) {
                // This should always be the default value despite the actual value of className - that is used only in "test-pv" intentionally
                pv.withStorageClassName("standard");
            }
            pv.endSpec().done();
        }
    }

    @When("create pull secret for backup")
    public void createPullSecretForBackup() {
        Account aws = AccountsDirectory.getInstance().get(Account.Name.AWS);
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

    @Then("wait for backup with {int}s interval")
    public void waitForBackup(int interval) {
        log.info("Waiting until the operator does the backup...");
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs("syndesis-operator").contains("backup for syndesis done"),
                interval * 1000L, 10 * 60000L);
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

    /**
     * Checks whether pods contains metering labels with correct values.
     * It is a feature for Fuse Online product, therefore check runs only in case of the productized build.
     */
    @Then("check that metering labels have correct values for \"([^\"]*)\"$")
    public void checkThatMeteringLabelsHaveCorrectValues(Component component)  {
        final String version = "8.0";
        final String company = "Red_Hat";
        final String prodName = "Red_Hat_Integration";
        final String componentName = "Fuse";
        final String subcomponent_t = "infrastructure";

        List<Pod> pods = OpenShiftUtils.getInstance().pods().withLabel("syndesis.io/component").list().getItems().stream()
            .filter(p -> !"integration".equals(p.getMetadata().getLabels().get("syndesis.io/component")))
            .collect(Collectors.toList());

        for (Pod p : pods) {
            if (p.getStatus().getPhase().contains("Running") && p.getMetadata().getName().contains(component.getName())) {
                Map<String, String> labels = p.getMetadata().getLabels();
                assertThat(labels).containsKey("com.company");
                assertThat(labels).containsKey("rht.prod_name");
                assertThat(labels).containsKey("rht.prod_ver");
                assertThat(labels).containsKey("rht.comp");
                assertThat(labels).containsKey("rht.comp_ver");
                assertThat(labels).containsKey("rht.subcomp");
                assertThat(labels).containsKey("rht.subcomp_t");

                assertThat(labels.get("com.company")).isEqualTo(company);
                assertThat(labels.get("rht.prod_name")).isEqualTo(prodName);
                assertThat(labels.get("rht.prod_ver")).isEqualTo(version);
                assertThat(labels.get("rht.comp")).isEqualTo(componentName);
                assertThat(labels.get("rht.comp_ver")).isEqualTo(version);
                assertThat(labels.get("rht.subcomp")).isEqualTo(component.getName());
                assertThat(labels.get("rht.subcomp_t")).isEqualTo(subcomponent_t);
            }
        }
    }

    @Then("verify whether operator metrics endpoint is active")
    public void checkEndpoint() {
        Endpoints operatorEndpoint = OpenShiftUtils.getInstance().getEndpoint("syndesis-operator-metrics");
        assertThat(operatorEndpoint.getSubsets()).isNotEmpty();
    }

    @Then("verify whether operator metrics endpoint includes version information")
    public void checkMetricsVersion() {
        try (LocalPortForward ignored = OpenShiftUtils.createLocalPortForward(
            //skip syndesis-operator-{d}-deploy pods
            OpenShiftUtils.getPod(p -> p.getMetadata().getName().matches("syndesis-operator-\\d-(?!deploy).*")), 8383, 8383)) {
            assertThat(HTTPUtils.doGetRequest("http://localhost:8383/metrics").getBody()).contains("syndesis_version_info{operator_version");
        } catch (IOException e) {
            fail("Unable to create port forward: ", e);
        }
    }

    @Then("check that the build config {string} contains variables:")
    public void checkBcVariables(String bcName, DataTable variables) {
        assertThat(OpenShiftUtils.getInstance().getBuildConfigEnvVars(bcName)).containsAllEntriesOf(variables.asMap(String.class, String.class));
    }

    @Then("check that the build log {string} contains {string}")
    public void checkBuildLog(String buildName, String expected) {
        assertThat(OpenShiftUtils.getInstance().getBuildLog(OpenShiftUtils.getInstance().getLatestBuild(buildName))).contains(expected);
    }

    @When("^check (affinity|tolerations)( not set)? for (infra|integration) pods$")
    public void checkAffinity(String test, String notSet, String method) {
        List<Pod> pods = "infra".equals(method)
            ? ComponentUtils.getComponentPods().stream().filter(p -> !p.getMetadata().getName().contains("operator")).collect(Collectors.toList())
            : OpenShiftUtils.findPodsByPredicates(p -> "integration".equals(p.getMetadata().getLabels().get("syndesis.io/type")));
        for (Pod p : pods) {
            String name = p.getMetadata().getName();
            if ("affinity".equals(test)) {
                Affinity podAffinity = p.getSpec().getAffinity();
                if (notSet == null) {
                    assertThat(podAffinity).as(name + ": affinity is null").isNotNull();
                    NodeAffinity nodeAffinity = podAffinity.getNodeAffinity();
                    assertThat(nodeAffinity).as(name + ": node affinity is null").isNotNull();
                    NodeSelector selector = nodeAffinity.getRequiredDuringSchedulingIgnoredDuringExecution();
                    assertThat(selector).as(name + ": required is null").isNotNull();
                    List<NodeSelectorTerm> terms = selector.getNodeSelectorTerms();
                    assertThat(terms).as(name + ": node selector is null").isNotNull();
                    assertThat(terms).as(name + ": node selector size isn't 1").hasSize(1);
                } else {
                    assertThat(podAffinity).isNull();
                }
            } else {
                Optional<Toleration> toleration = p.getSpec().getTolerations().stream()
                    .filter(t -> "node.kubernetes.io/network-unavailable".equals(t.getKey())).findAny();
                if (notSet == null) {
                    assertThat(toleration).as(name + ": Expected toleration setting is not present").isPresent();
                } else {
                    assertThat(toleration).as(name + ": Toleration shouldn't be present").isNotPresent();
                }
            }
        }
    }
}
