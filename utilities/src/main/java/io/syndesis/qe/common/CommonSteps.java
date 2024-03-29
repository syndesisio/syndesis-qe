package io.syndesis.qe.common;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.component.ComponentUtils;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.PortForwardUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.concurrent.TimeoutException;

import cz.xtf.core.waiting.Waiter;
import cz.xtf.core.waiting.WaiterException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonSteps {
    public static void cleanNamespace() {
        log.info("Cleaning namespace");
        undeploySyndesis();
        OpenShiftUtils.getInstance().templates().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().apps().statefulSets().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().apps().deployments().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().serviceAccounts().withName("syndesis-oauth-client").delete();
        try {
            Waiter clean = OpenShiftUtils.getInstance().clean();
            // workaround for Jaeger installed by OLM
            OpenShiftUtils.binary().execute("delete", "subscriptions", "jaeger-product");
            OpenShiftUtils.binary().execute("delete", "csv", "jaeger-operator.v1.20.3");
            clean.waitFor();
        } catch (WaiterException e) {
            log.warn("Project was not clean after 20s, retrying once again");
            OpenShiftUtils.getInstance().clean().waitFor();
        }
        OpenShiftUtils.getInstance().getTemplates().forEach(OpenShiftUtils.getInstance()::deleteTemplate);
        PortForwardUtils.reset();
    }

    public static void deploySyndesis() {
        log.info("Deploying Syndesis to namespace");
        ResourceFactory.get(Syndesis.class).deploy();
        // Use this method instead of ResourceFactory#create() to get the info what went wrong
        waitForSyndesis();
    }

    public static void waitForSyndesis() {
        try {
            log.info("Waiting for Syndesis to get ready");
            OpenShiftWaitUtils.waitFor(() -> ResourceFactory.get(Syndesis.class).isReady(), 10000L, 15 * 60000L);
            PortForwardUtils.createOrCheckPortForward();
        } catch (TimeoutException | InterruptedException e) {
            log.error("Was waiting for following syndesis components:");
            ComponentUtils.getAllComponents().forEach(c -> log.error("  " + c.getName()));
            log.error("Found following component pods:");
            ComponentUtils.getComponentPods().forEach(p -> log.error("  " + p.getMetadata().getName()
                + " [ready: " + OpenShiftWaitUtils.isPodReady(p) + "]"));
            InfraFail.fail("Wait for Syndesis failed, check error logs for details.", e);
        }
    }

    public static void undeploySyndesis() {
        ResourceFactory.get(Syndesis.class).undeployCustomResources();
        try {
            OpenShiftWaitUtils.waitFor(() -> ResourceFactory.get(Syndesis.class).isUndeployed(), 10 * 60000L);
        } catch (TimeoutException | InterruptedException e) {
            log.error("Was waiting until there is only operator pod or no pods");
            log.error("Found following component pods:");
            ComponentUtils.getComponentPods().forEach(p -> log.error("  " + p.getMetadata().getName()));
            InfraFail.fail("Wait for Syndesis undeployment failed, check error logs for details.", e);
        }
    }
}
