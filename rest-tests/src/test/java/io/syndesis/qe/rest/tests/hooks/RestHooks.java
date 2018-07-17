package io.syndesis.qe.rest.tests.hooks;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestHooks {
    @After
    public void getLogs(Scenario scenario){
        if (scenario.isFailed()) {
            log.warn("Scenario {} failed, saving server logs and integration logs to scenario", scenario.getName());
            scenario.embed(OpenShiftUtils.getInstance().getPodLog(OpenShiftUtils.getPodByPartialName("syndesis-server")).getBytes(), "text/plain");
            scenario.embed(OpenShiftUtils.getInstance().getPodLog(OpenShiftUtils.getPodByPartialName("i-")).getBytes(), "text/plain");
        }
    }
}
