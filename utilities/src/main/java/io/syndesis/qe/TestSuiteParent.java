package io.syndesis.qe;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.common.CommonSteps;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.SyndesisDB;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.PortForwardUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TestSuiteParent {
    @BeforeClass
    public static void beforeTests() {
        Runtime.getRuntime().addShutdownHook(new Thread(ResourceFactory::cleanup));
        try {
            // Do this check only if installing syndesis
            if (TestConfiguration.namespaceCleanup() && !TestUtils.isUserAdmin(TestConfiguration.adminUsername())) {
                throw new IllegalArgumentException("Admin user " + TestUtils.getCurrentUser()
                    + " specified in test properties doesn't have admin priviledges (if this shouldn't happen, check debug logs for more info");
            }

            if (TestUtils.isUserAdmin(TestConfiguration.syndesisUsername()) && !TestConfiguration.isSingleUser()) {
                throw new IllegalArgumentException("Syndesis user " + TestConfiguration.syndesisUsername() + " shouldn't have admin priviledges");
            }

            if (TestConfiguration.enableTestSupport()) {
                log.info("Enabling test support");
                if (OpenShiftUtils.dcContainsEnv("syndesis-server", "ENDPOINTS_TEST_SUPPORT_ENABLED") &&
                    OpenShiftUtils.envInDcContainsValue("syndesis-server", "ENDPOINTS_TEST_SUPPORT_ENABLED", "true")) {
                    log.info("TEST_SUPPORT is already enabled");
                } else {
                    // it doesn't work for Syndesis installed via OperatorHub since there is using Deployments and it is managed by
                    // ClusterServiceVersion
                    try {
                        OpenShiftUtils.updateEnvVarInDeploymentConfig("syndesis-operator", "TEST_SUPPORT", "true");
                    } catch (NullPointerException ex) {
                        fail(
                            "Syndesis-operator doesn't exist. The Syndesis is probably installed via OperatorHub. In that case you need to edit " +
                                "ClusterServiceVersion manually");
                    }
                    try {
                        OpenShiftWaitUtils.waitForPodIsReloaded("syndesis-operator");
                        OpenShiftWaitUtils.waitForPodIsReloaded("syndesis-server");
                        CommonSteps.waitForSyndesis();
                    } catch (InterruptedException | TimeoutException e) {
                        InfraFail.fail("Wait for Syndesis failed, check error logs for details.", e);
                    }
                }
            }

            if (!TestConfiguration.namespaceCleanup()) {
                if (OpenShiftUtils.getPodByPartialName("syndesis-server").isPresent() &&
                    !OpenShiftUtils.getPodByPartialName("syndesis-db").isPresent()) {
                    //syndesis server is in the namespace but the syndesis-db is not. Needs to deploy our own syndesis db and update
                    // default connection
                    ResourceFactory.create(SyndesisDB.class);
                }
                PortForwardUtils.createOrCheckPortForward();
                return;
            }

            if (OpenShiftUtils.getInstance().getProject(TestConfiguration.openShiftNamespace()) == null) {
                OpenShiftUtils.asRegularUser(() -> OpenShiftUtils.getInstance().createProjectRequest(TestConfiguration.openShiftNamespace()));
                TestUtils.sleepIgnoreInterrupt(10 * 1000L);
            }

            // You can't create project with annotations/labels - https://github.com/openshift/origin/issues/3819
            // So add them after the project is created
            // @formatter:off
            Map<String, String> labels = TestUtils.map("syndesis-qe/lastUsedBy", System.getProperty("user.name"));
            OpenShiftUtils.getInstance().namespaces().withName(TestConfiguration.openShiftNamespace()).edit()
                .editMetadata()
                .addToLabels(labels)
                .endMetadata()
                .done();
            // @formatter:on
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
        }
    }
}
