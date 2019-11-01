package io.syndesis.qe;

import io.syndesis.qe.bdd.CommonSteps;
import io.syndesis.qe.templates.KafkaTemplate;
import io.syndesis.qe.templates.KuduRestAPITemplate;
import io.syndesis.qe.templates.KuduTemplate;
import io.syndesis.qe.templates.MongoDb36Template;
import io.syndesis.qe.templates.WildFlyTemplate;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.Secret;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TestSuiteParent {

    private static Secret lockSecret;

    @BeforeClass
    public static void lockNamespace() throws InterruptedException {
        if (TestConfiguration.enableTestSupport()) {
            log.info("enabling test support");
            OpenShiftUtils.updateEnvVarInDeploymentConfig("syndesis-server", "ENDPOINTS_TEST_SUPPORT_ENABLED", "true");
            log.info("waiting for syndesis");
            Thread.sleep(10 * 1000);
            CommonSteps.waitForSyndesis();
        }

        if (!TestConfiguration.namespaceLock()) {
            return; //skip when syndesis.config.openshift.namespace.lock is false
        }
        if (OpenShiftUtils.xtf().getProject(TestConfiguration.openShiftNamespace()) == null) {
            OpenShiftUtils.xtf().createProjectRequest(TestConfiguration.openShiftNamespace());
            Thread.sleep(10 * 1000);
        }
        log.info("Waiting to obtain namespace lock");
        boolean isReady = TestUtils.waitForEvent(s -> !s.isPresent(),
            () -> OpenShiftUtils.getInstance().getSecrets().stream().filter(s -> "test-lock".equals(s.getMetadata().getName())).findFirst(),
            TimeUnit.MINUTES, 60,
            TimeUnit.SECONDS, 15);

        if (isReady) {
            log.info("No lock present, namespace is ready");
        } else {
            // there's probably staled lock after 60 min timeout,
            // display log warning and continue with force break
            log.warn("Can't obtain lock gracefully");
        }
        try {
            cleanNamespace();
        } catch (Exception e) {
            // When the test fails in @BeforeClass, the stacktrace is not printed and we get only this chain:
            // CucumberTest>TestSuiteParent.lockNamespace:53->TestSuiteParent.cleanNamespace:92 » NullPointer
            e.printStackTrace();
            throw e;
        }
        log.info("Creating namespace lock via secret `test-lock`");
        lockSecret = OpenShiftUtils.getInstance().secrets()
            .createOrReplaceWithNew()
            .withNewMetadata()
            .withName("test-lock")
            .endMetadata()
            .done();
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
        if (TestConfiguration.namespaceCleanup()) {
            CommonSteps commonSteps = new CommonSteps();
            log.info("Cleaning namespace");
            commonSteps.cleanNamespace();
            log.info("Deploying Syndesis to namespace");
            commonSteps.deploySyndesis();
            log.info("Waiting for Syndesis to get ready");
            commonSteps.waitForSyndesis();
        }
    }
}
