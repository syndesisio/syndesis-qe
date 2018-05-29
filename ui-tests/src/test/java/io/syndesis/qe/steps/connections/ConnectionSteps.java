package io.syndesis.qe.steps.connections;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.connections.wizard.phases.ConfigureConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.NameConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.SelectConnectionTypeSteps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionSteps {

    private Connections connectionsPage = new Connections();

    @Autowired
    private SelectConnectionTypeSteps selectConnectionTypeSteps;

    @When("^.*opens? the \"([^\"]*)\" connection detail$")
    public void openConnectionDetail(String connectionName) {
        connectionsPage.openConnectionDetail(connectionName);
    }

    @Then("^check visibility of the \"(\\w+)\" connection$")
    public void checkConnectionIsPresent(String connectionName) {
        connectionsPage.getConnection(connectionName).shouldBe(visible);
    }

    @Then("^check that \"([^\"]*)\" connection is not visible$")
    public void checkConnectionIsNotPresent(String connectionName) {
        connectionsPage.getConnection(connectionName).shouldNot(exist);
    }

    @When("^delete the \"([^\"]*)\" connection$")
    public void deleteConnection(String connectionName) {
        connectionsPage.deleteConnection(connectionName);
    }

    @When("^clicks? on the kebab menu icon of each available connection$")
    public void clickAllKebabMenus() {
        for(SelenideElement connection: connectionsPage.getAllConnections()) {
            new KebabMenu(connection.$(By.xpath(".//button")).shouldBe(visible)).open();
        }
    }

    @Then("^check visibility of \"(\\d+)\" connections$")
    public void connectionsCount(Integer connectionCount) {
        log.info("There should be {} available", connectionCount);
        assertThat(connectionsPage.getAllConnections().size(), is(connectionCount));
    }

    @Then("^check visibility of unveiled kebab menu of all connections, each of this menu consist of \"(\\w+)\", \"(\\w+)\" and \"(\\w+)\" actions$")
    public void checkAllVisibleKebabMenus(String action1, String action2, String action3) {
        List<String> actions = new ArrayList<>(Arrays.asList(action1, action2, action3));
        for(SelenideElement connection: connectionsPage.getAllConnections()) {
            KebabMenu kebabMenu = new KebabMenu(connection.$(By.xpath(".//button")).shouldBe(visible));

            for(String item: actions) {
                kebabMenu.getItemElement(item).shouldBe(visible);
            }

            kebabMenu.close();
        }
    }

    @Then("^check visibility of help block with text \"([^\"]*)\"$")
    public void helpBlockVisible(String helpText) {
        SelenideElement helpBlock = $(By.className("help-block")).shouldBe(visible);
        helpBlock.shouldBe(visible);
        Assertions.assertThat(helpBlock.getText().equals(helpText)).isTrue();
    }

    @When("^click on the \"([^\"]*)\" kebab menu button of \"([^\"]*)\"$")
    public void clickOnKebabMenuButtonOfConnection(String button, String connectionName) throws Throwable {
        KebabMenu kebabMenu = new KebabMenu(connectionsPage.getConnection(connectionName).$(By.xpath(".//button")).shouldBe(visible));
        kebabMenu.open();
        kebabMenu.getItemElement(button).shouldBe(visible).click();
    }

    /**
     * TODO: This is temporary solution. Unify with createConnections in CommonSteps
     */

    @Given("^creates connections without validation$")
    public void createConnectionsWithoutValidation(DataTable connectionsData) {
        Connections connectionsPage = new Connections();
        CommonSteps cs = new CommonSteps();

        ConfigureConnectionSteps configureConnectionSteps = new ConfigureConnectionSteps();
        NameConnectionSteps nameConnectionSteps = new NameConnectionSteps();

        List<List<String>> dataTable = connectionsData.raw();

        for (List<String> dataRow : dataTable) {
            String connectionType = dataRow.get(0);
            String connectionCredentialsName = dataRow.get(1);
            String connectionName = dataRow.get(2);
            String connectionDescription = dataRow.get(3);

            cs.navigateTo("Connections");
            cs.validatePage("Connections");

            ElementsCollection connections = connectionsPage.getAllConnections();
            connections = connections.filter(exactText(connectionName));

            if (connections.size() != 0) {
                log.warn("Connection {} already exists!", connectionName);
            } else {
                cs.clickOnButton("Create Connection");

                selectConnectionTypeSteps.selectConnectionType(connectionType);
                configureConnectionSteps.fillConnectionDetails(connectionCredentialsName);

                cs.scrollTo("top", "right");
                cs.clickOnButton("Next");

                nameConnectionSteps.setConnectionName(connectionName);
                nameConnectionSteps.setConnectionDescription(connectionDescription);

                cs.clickOnButton("Create");
            }
        }
    }
}
