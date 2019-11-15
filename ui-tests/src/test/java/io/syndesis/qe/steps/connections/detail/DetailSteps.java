package io.syndesis.qe.steps.connections.detail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.pages.connections.detail.ConnectionDetail;
import io.syndesis.qe.steps.CommonSteps;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetailSteps {

    private ConnectionDetail detailPage = new ConnectionDetail();

    @Then("^check visibility of \"([^\"]*)\" connection details")
    public void verifyConnectionDetails(String connectionName) {
        log.info("Connection detail page must show connection name");
        assertThat(detailPage.connectionName()).isEqualTo(connectionName);
    }

    @Then("^validate oauth connection \"([^\"]*)\" by clicking Validate button$")
    public void validateOauthConnectionByClickingValidateButton(String connectionName) {
        Connections connectionsPage = new Connections();
        connectionsPage.getConnection(connectionName).shouldBe(Condition.visible).click();

        new CommonSteps().clickOnButton("Validate");
        $(By.className("alert-success")).should(exist);
    }

    @When("change connection description to \"([^\"]*)\"")
    public void changeConnectionDescription(String connectionDescription) {
        detailPage.setDescription(connectionDescription);
    }

    @Then("^check that connection description \"([^\"]*)\"")
    public void verifyConnectionDescription(String description) {
        assertThat(detailPage.getDescription()).isEqualTo(description);
    }
}
