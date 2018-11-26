package io.syndesis.qe.steps.connections.detail;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.pages.connections.detail.ConnectionDetail;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class DetailSteps {

    private ConnectionDetail detailPage = new ConnectionDetail();

    @Then("^check visibility of \"([^\"]*)\" connection details")
    public void verifyConnectionDetails(String connectionName) {
        log.info("Connection detail page must show connection name");
        assertThat(detailPage.connectionName(), is(connectionName));
    }

    @Then("^validate oauth connection \"([^\"]*)\" by clicking Validate button$")
    public void validateOauthConnectionByClickingValidateButton(String connectionName) {
        Connections connectionsPage = new Connections();
        SelenideElement connectionElement = connectionsPage.getConnection(connectionName);
        connectionElement.shouldBe(Condition.visible).click();

        SelenideElement validationElement = detailPage.getRootElement().$(By.tagName("syndesis-connection-configuration-validation"));
        validationElement.shouldBe(Condition.visible);
        SelenideElement validateButton = validationElement.$(By.tagName("button")).shouldBe(Condition.visible);
        assertThat("Validate Button couldn't be found.", validateButton, notNullValue());
        assertThat("Validate button should be enabled", validateButton.isEnabled(), is(true));
        validateButton.click();
    }

}
