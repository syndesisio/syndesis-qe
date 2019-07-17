package io.syndesis.qe.steps.customizations.connectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.pages.customizations.connectors.ApiClientConnectors;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.ReviewActions;
import io.syndesis.qe.utils.UploadFile;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiClientConnectorsSteps {

    private static ApiClientConnectors apiClientConnectorsPage = new ApiClientConnectors();
    private static ReviewActions reviewActions = new ReviewActions();

    @Then("^open new Api Connector wizard$")
    public void openNewApiConnectorWizard() {
        apiClientConnectorsPage.startWizard();
    }

    @When("^set swagger petstore credentials")
    public void setPetstoreCredentials() {
        Account petStoreAccount = new Account();
        petStoreAccount.setService("Swagger Petstore");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("authenticationparametervalue", "special-key");
        petStoreAccount.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount("Swagger Petstore Account", petStoreAccount);
    }

    @Then("^open the API connector \"([^\"]*)\" detail$")
    public void openApiConnectorDetail(String connectorName) {
        apiClientConnectorsPage.clickConnectorByTitle(connectorName);
    }

    @Then("^check visibility of the new connector \"([^\"]*)\"$")
    public void checkNewConnectorIsPresent(String connectorName) throws Throwable {
        assertTrue("Connector [" + connectorName + "] is not listed.", apiClientConnectorsPage.isConnectorPresent(connectorName));
    }

    @Then("^check visibility of a connectors list of size (\\d+)$")
    public void checkConnectorsListSize(int listSize) {
        assertTrue("The connectors list should be of size <" + listSize + ">.", apiClientConnectorsPage.isConnectorsListLongAs(listSize));
    }

    @Then("^checks? the error box (is|is not) present$")
    public void checkErrorBoxPresent(String present) {
        if ("is".equals(present)) {
            log.info("checking if ERRORBOX is visible");
            assertTrue("The validation error box should exist", reviewActions.getValidationErrorBox().is(visible));
        } else {
            log.info("checking if ERRORBOX is not visible");
            assertTrue("The validation error box should not exist", reviewActions.getValidationErrorBox().is(not(visible)));
        }
    }

    @Then("^delete connector([^\"]*)$")
    public void deleteConnector(String name) {
        apiClientConnectorsPage.getDeleteButton(name).shouldBe(visible).click();
    }

    //***************************************************************************
    //******************************* bulk steps ********************************
    //***************************************************************************

    @Then("^upload swagger file (.+)$")
    public void uploadSwaggerFile(String filePath) {
        log.debug("File path: " + filePath);
        ElementsCollection col;
        try {
            try {
                OpenShiftWaitUtils.waitFor(() -> $$(By.tagName("input")).filter(attribute("type", "file")).size() == 1, 20 * 1000L);
            } catch (TimeoutException | InterruptedException e) {
                fail("Can not upload swagger - input button was not found");
            }
            col = $$(By.tagName("input")).filter(attribute("type", "file"));
            assertThat(col).size().isEqualTo(1);
            UploadFile.uploadFile(col.get(0), new File(filePath));
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            log.error("An error happened, retrying once");
            col = $$(By.tagName("input")).filter(attribute("type", "file"));
            assertThat(col).size().isEqualTo(1);
            UploadFile.uploadFile(col.get(0), new File(filePath));
        }
    }
}
