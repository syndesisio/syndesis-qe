package io.syndesis.qe;

import static io.syndesis.qe.util.HelperFunctions.copyManifestFilestFromImage;
import static io.syndesis.qe.util.HelperFunctions.doDeleteRequest;
import static io.syndesis.qe.util.HelperFunctions.doPostRequest;
import static io.syndesis.qe.util.HelperFunctions.encodeFileToBase64Binary;
import static io.syndesis.qe.util.HelperFunctions.getOperatorName;
import static io.syndesis.qe.util.HelperFunctions.getOperatorVersion;
import static io.syndesis.qe.util.HelperFunctions.getPackageName;
import static io.syndesis.qe.util.HelperFunctions.linkPullSecret;
import static io.syndesis.qe.util.HelperFunctions.readResource;
import static io.syndesis.qe.util.HelperFunctions.replaceImageInManifest;

import static org.assertj.core.api.Assertions.fail;

import static com.amazonaws.util.StringUtils.UTF8;

import io.syndesis.qe.tar.Compress;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.codeborne.selenide.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.extern.slf4j.Slf4j;

@RunWith(Cucumber.class)
@Slf4j
@CucumberOptions(
    features = "classpath:features",
    tags = {"not @wip", "not @manual", "not @deprecated", "not @disabled"},
    plugin = {
        "pretty",
        "html:target/cucumber/cucumber-html",
        "junit:target/cucumber/cucumber-junit.xml",
        "json:target/cucumber/cucumber-report.json",
        "io.syndesis.qe.cucumber.MailFormatter:target/cucumber/cucumber-mail/"
    }
)

public class MarketplaceTest {

    private static String quayPackage;

    private static final String QUAY_LOGIN_URL = "https://quay.io/cnr/api/v1/users/login";
    private static final String QUAY_PUSH_URL = "https://quay.io/cnr/api/v1/packages/" + TestConfiguration.quayNamespace();
    private static final String QUAY_REPOSITORY_URL =  "https://quay.io/api/v1/repository/" +
        TestConfiguration.quayNamespace() + "/" + "QUAY_PACKAGE";
    private static final String QUAY_CHANGE_VISIBILITY_URL = QUAY_REPOSITORY_URL + "/changevisibility";

    @BeforeClass
    public static void setupMarketplace() throws IOException {
        quayPackage = createQuayProject();

        deployOperatorToOpenshhift(quayPackage);
        setupCucumber();
    }

    @AfterClass
    public static void tearDownMarketplace() throws IOException {
        if (StringUtils.isNotEmpty(quayPackage)) {
            log.info("Deleting operator source for quay package '" + quayPackage + "'");
            CustomResourceDefinitionContext operatorSourceCrdContext = new CustomResourceDefinitionContext.Builder()
                .withGroup("operators.coreos.com")
                .withPlural("operatorsources")
                .withScope("Namespaced")
                .withVersion("v1")
                .build();

            OpenShiftUtils.getInstance().customResource(operatorSourceCrdContext)
                .delete("openshift-marketplace", quayPackage);

            log.info("Deleting project from quay");
            String botToken = "Bearer " + TestConfiguration.quayAuthToken();
            doDeleteRequest(QUAY_REPOSITORY_URL.replaceAll("QUAY_PACKAGE", quayPackage),
                null, botToken);
        }
    }

    private static void setupCucumber() {
        //set up Selenide
        Configuration.timeout = TestConfiguration.getConfigTimeout() * 1000;
        //We will now use custom web driver
        //Configuration.browser = TestConfiguration.syndesisBrowser();
        Configuration.browser = "io.syndesis.qe.CustomWebDriverProvider";
        Configuration.browserSize = "1920x1080";
    }

    private static void deployOperatorToOpenshhift(String packageName) throws IOException {

        log.info("Disabling default sources on openshift");

        CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
            .withGroup("config.openshift.io")
            .withPlural("operatorhubs")
            .withScope("Cluster")
            .withVersion("v1")
            .build();

        OpenShiftUtils.getInstance().customResource(crdContext)
            .createOrReplace("openshift-marketplace",
                MarketplaceTest.class.getResourceAsStream("/openshift/disable-default-sources.yaml"));


        log.info("Creating operator source which points toward quay");

        CustomResourceDefinitionContext operatorSourceCrdContext = new CustomResourceDefinitionContext.Builder()
            .withGroup("operators.coreos.com")
            .withPlural("operatorsources")
            .withScope("Namespaced")
            .withVersion("v1")
            .build();

        String operatorSourceYaml = readResource("openshift/create-operatorsource.yaml")
            .replaceAll("PACKAGE_NAME", packageName)
            .replaceAll("QUAY_NAMESPACE", TestConfiguration.quayNamespace());

        OpenShiftUtils.getInstance().customResource(operatorSourceCrdContext)
            .createOrReplace("openshift-marketplace",
                new ByteArrayInputStream(operatorSourceYaml.getBytes(StandardCharsets.UTF_8)));

        if (OpenShiftUtils.getInstance().getProject(TestConfiguration.openShiftNamespace()) != null) {
            log.info("Namespace exists, deleting namespace first");
            OpenShiftUtils.getInstance().deleteProject(TestConfiguration.openShiftNamespace());
            TestUtils.waitFor(
                () -> OpenShiftUtils.getInstance().getProject(TestConfiguration.openShiftNamespace()) == null,
                1, 30,
                "Namespace was not deleted");
        }

        log.info("Creating namespace");

        OpenShiftUtils.asRegularUser(() -> OpenShiftUtils.getInstance().createProjectRequest(TestConfiguration.openShiftNamespace()));
        TestUtils.sleepIgnoreInterrupt(10 * 1000L);

        log.info("Creating pull secret");

        if (TestConfiguration.syndesisPullSecret() != null) {
            log.info("Creating a pull secret with name " + TestConfiguration.syndesisPullSecretName());
            OpenShiftUtils.getInstance().secrets().createOrReplaceWithNew()
                .withNewMetadata()
                .withName(TestConfiguration.syndesisPullSecretName())
                .endMetadata()
                .withData(TestUtils.map(".dockerconfigjson", TestConfiguration.syndesisPullSecret()))
                .withType("kubernetes.io/dockerconfigjson")
                .done();
        }

        log.info("Creating operatorgroup");

        CustomResourceDefinitionContext operatorGroupCrdContext = new CustomResourceDefinitionContext.Builder()
            .withGroup("operators.coreos.com")
            .withPlural("operatorgroups")
            .withScope("Namespaced")
            .withVersion("v1alpha2")
            .build();

        String operatorGroupYaml = readResource("openshift/create-operatorgroup.yaml")
            .replaceAll("OPENSHIFT_PROJECT", TestConfiguration.openShiftNamespace());

        OpenShiftUtils.getInstance().customResource(operatorGroupCrdContext)
            .createOrReplace(TestConfiguration.openShiftNamespace(),
                             new ByteArrayInputStream(operatorGroupYaml.getBytes(UTF8)));


        log.info("Creating operator subscription");

        String subscriptionYaml = readResource("openshift/create-subscription.yaml")
            .replaceAll("PACKAGE_NAME", packageName)
            .replaceAll("OPENSHIFT_PROJECT", TestConfiguration.openShiftNamespace());

        CustomResourceDefinitionContext subscriptionCrdContext = new CustomResourceDefinitionContext.Builder()
            .withGroup("operators.coreos.com")
            .withPlural("subscriptions")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .build();

        OpenShiftUtils.getInstance().customResource(subscriptionCrdContext)
            .createOrReplace(TestConfiguration.openShiftNamespace(),
                new ByteArrayInputStream(subscriptionYaml.getBytes(StandardCharsets.UTF_8)));

        TestUtils.waitFor(() ->
            OpenShiftUtils.getInstance().inNamespace(TestConfiguration.openShiftNamespace()).pods().list().getItems().size() == 1,
            1, 30,
            "Must be one pod in the project at this point"
        );

        DeploymentList deploymentList =
            OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).list();
        if (deploymentList.getItems().size() != 1) {
            fail("Must be one deployment, actual number is " + deploymentList.getItems().size());
        }

        String operatorResourcesName = deploymentList.getItems().get(0).getMetadata().getName();

        log.info("Operator pod name is '" + operatorResourcesName + "'");

        log.info("Linking pull secret to service account user");

        linkPullSecret(operatorResourcesName);

        log.info("Redeploying operator pod so it uses new pull secret");

        OpenShiftUtils.getInstance()
            .apps().deployments().inNamespace(TestConfiguration.openShiftNamespace())
            .withName(operatorResourcesName).scale(0);
        TestUtils.waitFor(
            () -> OpenShiftUtils.getInstance().pods().inNamespace(TestConfiguration.openShiftNamespace())
                .list().getItems().size() == 0,
            1, 30,
            "Couldn't wait for pod to scale down"
        );

        OpenShiftUtils.getInstance()
            .apps().deployments().inNamespace(TestConfiguration.openShiftNamespace())
            .withName(operatorResourcesName).scale(1);
        TestUtils.waitFor(
            () -> OpenShiftUtils.getInstance().pods().inNamespace(TestConfiguration.openShiftNamespace())
                .list().getItems().size() == 1,
            1, 30,
            "Couldn't wait for pod to scale up"
        );
    }

    public static String createQuayProject() throws IOException {
        String operatorName = getOperatorName();
        Path tempDir = Files.createTempDirectory(operatorName);
        String operatorImage = TestConfiguration.syndesisOperatorImage();

        log.info("Cleaning temoporary directory " + tempDir.toString());

        FileUtils.cleanDirectory(tempDir.toFile());

        log.info("Acquiring manifests from operator image");

        String result = copyManifestFilestFromImage(operatorImage, tempDir.toString());
        if (StringUtils.isNotEmpty(result)) {
            log.error("Could not get files from image, result from docker command is:");
            log.error(result);
            throw new RuntimeException();
        }

        String packageName = getPackageName(tempDir.resolve("manifests"));
        String operatorVersion = getOperatorVersion(tempDir.resolve("manifests"));
        String tarFile = tempDir.toString() + "/" + packageName + ".tar.gz";

        log.info("Operator application name is '" + packageName + "'");
        log.info("Operator version is '" + operatorVersion + "'");

        Path fileToFix = tempDir.resolve("manifests").resolve(operatorVersion)
            .resolve(operatorName + ".v" + operatorVersion + ".clusterserviceversion.yaml");
        log.info("Fixing operator image in manifest file " + fileToFix.toString());

        replaceImageInManifest(fileToFix);

        String random = RandomStringUtils.random(8, true, true).toLowerCase();

        Compress compress = new Compress(tarFile, packageName + "-" + random);
        compress.writedir(tempDir.resolve("manifests"));
        compress.close();

        String base64 = encodeFileToBase64Binary(tarFile);

        log.info("Acquiring quay login data");

        String quayLoginRequest = readResource("quay/quay-login.json")
            .replaceAll("QUAY_USERNAME", TestConfiguration.quayUsername())
            .replaceAll("QUAY_PASSWORD", TestConfiguration.quayPassword());

        String quayLoginResponse = doPostRequest(QUAY_LOGIN_URL, quayLoginRequest, null);

        JSONObject jsonObject = new JSONObject(quayLoginResponse);
        String token = jsonObject.getString("token");

        log.info("Creating application on quay");

        String pushBody = readResource("quay/quay-push.json")
            .replaceAll("QUAY_PAYLOAD", base64)
            .replaceAll("QUAY_RELEASE", operatorVersion);

        doPostRequest(QUAY_PUSH_URL + "/" + packageName, pushBody, token);

        log.info("Changing quay application visibility to 'public'");

        pushBody = "{\"visibility\":\"public\"}";
        String botToken = "Bearer " + TestConfiguration.quayAuthToken();
        doPostRequest(QUAY_CHANGE_VISIBILITY_URL.replaceAll("QUAY_PACKAGE", packageName), pushBody, botToken);

        return packageName;
    }

}
