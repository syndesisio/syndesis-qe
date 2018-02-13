package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import cucumber.api.java.en.Then;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.MetricsEndpoint;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsValidationSteps {

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private MetricsEndpoint metricsEndpoint;

    public MetricsValidationSteps() {
    }

    @Then("^validate that number of all messages through integration \"([^\"]*)\" is greater than \"([^\"]*)\", period in ms: \"([^\"]*)\"$")
    public void validateThatNumberOfAllMessagesOfIntegrationIsGreaterThanPeriodInMs(String integrationName, Integer nr, Integer ms) throws InterruptedException {
        Thread.sleep(ms + 1000);
//        0.  get integration id.
        String id = this.getIdByIntegrationName(integrationName);
        Assertions.assertThat(id).isNotNull();

//        1. get metrics:
        IntegrationMetricsSummary summary = metricsEndpoint.get(id);
//        2. get metrics info:
        Long nrOfMessages = summary.getMessages().get();
        log.info("MESSAGES SUMMARY: *{}*", nrOfMessages);
        Assertions.assertThat(nrOfMessages).isEqualTo(3);

        Date lastProceded = summary.getLastProcessed().get();
        log.info("LAST MESSAGE WAS PROCEEDED ON: *{}*", lastProceded);
    }

//    AUXILIARIES
    private String getIdByIntegrationName(String integrationName) {
        List<Integration> integrations = integrationsEndpoint.list();
        Integration integr = integrations.stream().filter(integration -> integrationName.equals(integration.getName())).findAny().orElse(null);
        return integr.getId().get();
    }
}
