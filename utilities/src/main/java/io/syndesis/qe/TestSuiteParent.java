package io.syndesis.qe;

import io.syndesis.qe.bdd.CommonSteps;
import io.syndesis.qe.templates.CamelK;
import io.syndesis.qe.templates.Jaeger;
import io.syndesis.qe.templates.KafkaTemplate;
import io.syndesis.qe.templates.KuduRestAPITemplate;
import io.syndesis.qe.templates.KuduTemplate;
import io.syndesis.qe.templates.MongoDb36Template;
import io.syndesis.qe.templates.WildFlyTemplate;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TestSuiteParent {
    @BeforeClass
    public static void beforeTests() {
        // Do this check only if installing syndesis
        if (TestConfiguration.namespaceCleanup() && !TestUtils.isUserAdmin(TestConfiguration.adminUsername())) {
            throw new IllegalArgumentException("Admin user " + TestUtils.getCurrentUser()
                + " specified in test properties doesn't have admin priviledges");
        }

        if (TestUtils.isUserAdmin(TestConfiguration.syndesisUsername())) {
            throw new IllegalArgumentException("Syndesis user " + TestConfiguration.syndesisUsername() + " shouldn't have admin priviledges");
        }

        if (TestConfiguration.enableTestSupport()) {
            log.info("Enabling test support");
            OpenShiftUtils.updateEnvVarInDeploymentConfig("syndesis-server", "ENDPOINTS_TEST_SUPPORT_ENABLED", "true");
            log.info("Waiting for syndesis");
            TestUtils.sleepIgnoreInterrupt(10 * 1000L);
            CommonSteps.waitForSyndesis();
        }

        if (!TestConfiguration.namespaceCleanup()) {
            return;
        }

        if (OpenShiftUtils.getInstance().getProject(TestConfiguration.openShiftNamespace()) == null) {
            OpenShiftUtils.asRegularUser(() -> OpenShiftUtils.getInstance().createProjectRequest(TestConfiguration.openShiftNamespace()));
            TestUtils.sleepIgnoreInterrupt(10 * 1000L);
        }

        try {
            cleanNamespace();
            deploySyndesis();
        } catch (Exception e) {
            // When the test fails in @BeforeClass, the stacktrace is not printed and we get only this chain:
            // CucumberTest>TestSuiteParent.lockNamespace:53->TestSuiteParent.cleanNamespace:92 » NullPointer
            e.printStackTrace();
            throw e;
        }
    }

    @AfterClass
    public static void tearDown() {
        if (TestUtils.isDcDeployed("syndesis-kudu")) {
            log.info("Cleaning Kudu instances");
            KuduRestAPITemplate.cleanUp();
            KuduTemplate.cleanUp();
        }

        if (TestUtils.isDcDeployed("odata")) {
            WildFlyTemplate.cleanUp("odata");
        }

        if (TestUtils.isDcDeployed(MongoDb36Template.APP_NAME)) {
            MongoDb36Template.cleanUp();
        }

        if (OpenShiftUtils.podExists(p -> p.getMetadata().getName().contains("strimzi-cluster-operator"))) {
            KafkaTemplate.undeploy();
        }

        if (OpenShiftUtils.podExists(p -> p.getMetadata().getName().startsWith("camel-k-operator"))) {
            CamelK.undeploy();
        }

        if (OpenShiftUtils.podExists(p -> p.getMetadata().getName().startsWith("jaeger-operator"))) {
            Jaeger.undeploy();
        }

        if (lockSecret != null) {
            if (TestConfiguration.namespaceCleanupAfter()) {
                log.info("Cleaning namespace");
                OpenShiftUtils.getInstance().clean();
            } else {
                log.info("Releasing namespace lock");
                OpenShiftUtils.getInstance().deleteSecret(lockSecret);
            }
        }
    }

    private static void cleanNamespace() {
        log.info("Cleaning namespace");
        CommonSteps.cleanNamespace();
    }

    private static void deploySyndesis() {
        if (TestConfiguration.namespaceCleanup()) {
            log.info("Deploying Syndesis to namespace");
            CommonSteps.deploySyndesis();
            log.info("Waiting for Syndesis to get ready");
            CommonSteps.waitForSyndesis();
        }
    }
}
