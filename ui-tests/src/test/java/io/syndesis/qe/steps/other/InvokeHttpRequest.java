package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import cucumber.api.java.en.When;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvokeHttpRequest {
    /**
     * @param webhookToken token set when creating the integration
     * @param body
     * @throws IOException
     */
    @When("^.*invoke post request to integration \"([^\"]*)\" with webhook \"([^\"]*)\" and body (.*)$")
    public void invokeRequest(String integrationName, String webhookToken, String body) {
        log.info("Body to set: " + body);
        //example of webhook url: "https://i-webhook-test-syndesis.192.168.42.2.nip.io/webhook/test-webhook"
        String combinedUrl = TestConfiguration.syndesisUrl()
                .replace("syndesis", "i-" + integrationName + "-syndesis") + "/webhook/" + webhookToken;
        log.info("Combined URL: " + combinedUrl);

        assertThat(HttpUtils.doPostRequest(combinedUrl, body).code())
                .isEqualTo(204);
    }

}
