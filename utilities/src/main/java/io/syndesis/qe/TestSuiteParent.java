package io.syndesis.qe;

import io.syndesis.qe.bdd.CommonSteps;
import io.syndesis.qe.resource.ResourceFactory;
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
        ResourceFactory.cleanup();
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
