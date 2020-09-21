package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.lang3.StringUtils;

import io.cucumber.java.en.Then;

public class KafkaValidationSteps {
    @Then("check that kafka option {string} is set to {string} in {string} integration")
    public void validateKafkaOptions(String key, String value, String integration) {
        try {
            OpenShiftWaitUtils.waitFor(() -> StringUtils.substringAfter(OpenShiftUtils.getIntegrationLogs(integration), "ConsumerConfig values")
                .contains(key), 5000L, 60000L);
        } catch (Exception e) {
            fail("Unable to find {} option in integration log", key);
        }
        assertThat(StringUtils.substringAfter(OpenShiftUtils.getIntegrationLogs(integration), "ConsumerConfig values")).contains(key + " = " + value);
    }
}
