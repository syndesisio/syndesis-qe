package io.syndesis.qe.steps.other;

import cucumber.api.java.en.When;
import io.syndesis.qe.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class InvokeHttpRequest {

    /**
     * Only works when you are currently on integration details page, because we have to get
     * webhook url from the page
     *
     * @param body
     */
    @When("^invoke post request to webhook with body (.*)$")
    public void invokeWebhookRequest(String body) {
        log.debug("Body to set: " + body);

        String url = $(By.className("pfng-block-copy-preview-txt")).shouldBe(visible).getText();

        log.info("WebHook URL: " + url);
        assertThat(HttpUtils.doPostRequest(url, body).code())
                .isEqualTo(204);
    }
}
