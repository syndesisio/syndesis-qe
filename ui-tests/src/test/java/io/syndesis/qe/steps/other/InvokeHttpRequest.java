package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;

import java.util.List;

import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvokeHttpRequest {

    /**
     * Only works when you are currently on integration details page, because we have to get
     * webhook url from the page
     *
     * @param body
     */
    @When("^invoke post request to webhook in integration (.*) with token (.*) and body (.*)$")
    public void invokeWebhookRequestCannotFail(String nameOfIntegration, String token, String body) {
        assertThat(this.invokeWebhookRequest(nameOfIntegration, token, body).getCode()).isEqualTo(204);
    }

    // data table values:
    // |integrationName |webhook-token  |request body   |expected output code   |
    @When("^invoke post request to webhook$")
    public void invokeWebhookRequestWithExpectedCode(DataTable data) {
        for (List<String> row : data.cells()) {
            assertThat(this.invokeWebhookRequest(row.get(0), row.get(1), row.get(2)).getCode())
                .isEqualTo(Integer.valueOf(row.get(3)));
        }
    }

    @When("^invoke post request which can fail to webhook in integration (.*) with token (.*) and body (.*)$")
    public void invokeWebhookRequestCanFail(String nameOfIntegration, String token, String body) {
        this.invokeWebhookRequest(nameOfIntegration, token, body);
    }

    private HTTPResponse invokeWebhookRequest(String nameOfIntegration, String token, String body) {
        log.debug("Body to set: " + body);
        String url = getUrlForWebhook(nameOfIntegration, token);
        log.info("WebHook URL: " + url);
        return HttpUtils.doPostRequest(url, body);
    }

    public static String getUrlForWebhook(String nameOfIntegration, String token) {
        return String.format("https://%s/webhook/%s",
            OpenShiftUtils.getInstance().getRoutes().stream()
                .filter(x -> x.getMetadata().getName()
                    .contains(nameOfIntegration
                        .replaceAll("_", "-")
                        .toLowerCase()
                    ))
                .findFirst().get().getSpec().getHost(), token);
    }
}
