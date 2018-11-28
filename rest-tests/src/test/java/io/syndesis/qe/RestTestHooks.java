package io.syndesis.qe;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import io.fabric8.kubernetes.api.model.Pod;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.SampleDbConnectionManager;
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

    @After
    public void afterTest() {
        stepStorage.flushStepDefinitions();
        log.debug("Flushed steps from steps storage");
        SampleDbConnectionManager.closeConnections();
    }

    @After("@upgrade,@rollback,@upgrade-operator")
    public void clearUpgrade() {
        // Restore syndesis version if it was changed by previous upgrade test
        if (System.getProperty("syndesis.upgrade.backup.version") != null) {
            System.setProperty("syndesis.version", System.getProperty("syndesis.upgrade.backup.version"));
        }
        System.clearProperty("syndesis.upgrade.version");
    }

    @After
    public void getLogs(Scenario scenario){
        if (scenario.isFailed()) {
            log.warn("Scenario {} failed, saving server logs and integration logs to scenario", scenario.getName());
            scenario.embed(OpenShiftUtils.getInstance().getPodLog(OpenShiftUtils.getPodByPartialName("syndesis-server").get()).getBytes(), "text/plain");
            // There can be multiple integration pods for one test
            List<Pod> integrationPods = OpenShiftUtils.client().pods().list().getItems().stream().filter(
                    p -> p.getMetadata().getName().startsWith("i-")
                    && !p.getMetadata().getName().contains("deploy")
                    && !p.getMetadata().getName().contains("build")
            ).collect(Collectors.toList());
            for (Pod integrationPod : integrationPods) {
                scenario.embed(String.format("%s\n\n%s", integrationPod.getMetadata().getName(),
                        OpenShiftUtils.getInstance().getPodLog(integrationPod)).getBytes(), "text/plain");
            }

        }
    }
}
