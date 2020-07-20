package io.syndesis.qe;

import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Jaeger;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.PortForwardUtils;

import io.cucumber.java.After;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CucumberHooks {
    @After
    public void reset() {
        // Each operator deployment creates a new deployment, so it is needed to terminate the port forward after each test
        PortForwardUtils.reset();
        // Delete all test PVs
        OpenShiftUtils.getInstance().persistentVolumes().list().getItems().stream()
            .filter(pv -> pv.getMetadata().getName().startsWith(OperatorValidationSteps.TEST_PV_NAME))
            .forEach(pv -> OpenShiftUtils.getInstance().persistentVolumes().withName(pv.getMetadata().getName()).cascading(true).delete());
    }

    @After("@operator-addons-jaeger-external")
    public void undeployJaeger() {
        log.info("Undeploying external Jaeger");
        ResourceFactory.get(Jaeger.class).undeploy();
    }
}
