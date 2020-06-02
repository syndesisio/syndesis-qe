package io.syndesis.qe;

import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.bdd.validation.OperatorValidationSteps;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Jaeger;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.SampleDbConnectionManager;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

import cucumber.api.java.After;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 19, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class RestTestHooks {

    @Autowired
    private StepsStorage stepStorage;

    @After("@sqs")
    public void clearSqsProperty() {
        System.clearProperty("sqs.batch");
    }

    @After
    public void afterTest() {
        stepStorage.flushStepDefinitions();
        log.debug("Flushed steps from steps storage");
        SampleDbConnectionManager.closeConnections();
    }

    @After("@operator")
    public void reset() {
        // Each operator deployment creates a new deployment, so it is needed to terminate the port forward after each test
        RestUtils.reset();
        // Delete all test PVs
        OpenShiftUtils.getInstance().persistentVolumes().list().getItems().stream()
            .filter(pv -> pv.getMetadata().getName().startsWith(OperatorValidationSteps.TEST_PV_NAME))
            .forEach(pv -> OpenShiftUtils.getInstance().persistentVolumes().withName(pv.getMetadata().getName()).cascading(true).delete());
    }

    @After("@publicapi-connections")
    public void resetPostgresDB() {
        log.info("Back default values of PostgresDB");
        TestSupport.getInstance().resetDB();
    }

    @After("@operator-addons-jaeger-external")
    public void undeployJaeger() throws TimeoutException, InterruptedException {
        log.info("Undeploying external Jaeger");
        ResourceFactory.get(Jaeger.class).undeploy();
    }
}
