package io.syndesis.qe;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.After;
import io.syndesis.qe.rest.tests.storage.StepsStorage;
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
    }
}
