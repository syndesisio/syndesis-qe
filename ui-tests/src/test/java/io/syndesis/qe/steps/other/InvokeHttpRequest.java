package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class InvokeHttpRequest {

    /**
     * Only works when you are currently on integration details page, because we have to get
     * webhook url from the page
     *
     * @param body
     */
    @When("^invoke post request to webhook with body (.*)$")
    public void invokeWebhookRequestCannotFail(String body) {
        assertThat(this.invokeWebhookRequest(body).getCode()).isEqualTo(204);
    }

    @When("^invoke post request which can fail to webhook with body (.*)$")
    public void invokeWebhookRequestCanFail(String body) {
        this.invokeWebhookRequest(body);
    }

    private HTTPResponse invokeWebhookRequest(String body) {
        log.debug("Body to set: " + body);
        String url = $(By.className("pfng-block-copy-preview-txt")).shouldBe(visible).getAttribute("value");
        log.info("WebHook URL: " + url);
        return HttpUtils.doPostRequest(url, body);
    }
}
