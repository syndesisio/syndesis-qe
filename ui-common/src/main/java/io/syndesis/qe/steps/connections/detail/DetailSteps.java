package io.syndesis.qe.steps.connections.detail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exist;

import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.pages.connections.detail.ConnectionDetail;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.utils.Alert;

import com.codeborne.selenide.Condition;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetailSteps {

    private ConnectionDetail detailPage = new ConnectionDetail();

    @Then("check visibility of {string} connection details")
    public void verifyConnectionDetails(String connectionName) {
        log.info("Connection detail page must show connection name");
        assertThat(detailPage.connectionName()).isEqualTo(connectionName);
    }

    @Then("validate oauth connection {string} by clicking Validate button")
    public void validateOauthConnectionByClickingValidateButton(String connectionName) {
        Connections connectionsPage = new Connections();
        connectionsPage.getConnection(connectionName).shouldBe(Condition.visible).click();

        new CommonSteps().clickOnButton("Validate");
        detailPage.getCloseableAllerts(Alert.SUCCESS).first().shouldBe(exist);
    }

    @Then("remove all {string} alerts")
    public void removeAllAlerts(String alertType) {
        try {
            detailPage.removeAllAlertsFromPage(Alert.getALERTS().get(alertType));
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            //            repeat everything again:
            detailPage.removeAllAlertsFromPage(Alert.getALERTS().get(alertType));
        }
    }

    @When("change connection description to {string}")
    public void changeConnectionDescription(String connectionDescription) {
        detailPage.setDescription(connectionDescription);
    }

    @Then("check that connection description {string}")
    public void verifyConnectionDescription(String description) {
        assertThat(detailPage.getDescription()).isEqualTo(description);
    }
}
