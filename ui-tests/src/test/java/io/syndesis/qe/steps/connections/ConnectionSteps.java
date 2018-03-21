package io.syndesis.qe.steps.connections;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.menu.KebabMenu;
import io.syndesis.qe.pages.connections.Connections;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionSteps {

    private Connections connectionsPage = new Connections();

    @When("^.*opens? the \"([^\"]*)\" connection detail$")
    public void openConnectionDetail(String connectionName) {
        connectionsPage.openConnectionDetail(connectionName);
    }

    @Then("^Camilla can see the \"(\\w+)\" connection$")
    public void checkConnectionIsPresent(String connectionName) {
        connectionsPage.getConnection(connectionName).shouldBe(visible);
    }

    @Then("^Camilla can not see the \"([^\"]*)\" connection anymore$")
    public void checkConnectionIsNotPresent(String connectionName) {
        connectionsPage.getConnection(connectionName).shouldNot(exist);
    }

    @When("^Camilla deletes the \"([^\"]*)\" connection$")
    public void deleteConnection(String connectionName) {
        connectionsPage.deleteConnection(connectionName);
    }

    @When("^clicks? on the kebab menu icon of each available connection$")
    public void clickAllKebabMenus() {
        for(SelenideElement connection: connectionsPage.getAllConnections()) {
            new KebabMenu(connection.$(By.xpath(".//button"))).open();
        }
    }

    @Then("^she is presented with \"(\\d+)\" connections$")
    public void connectionsCount(Integer connectionCount) {
        log.info("There should be {} available", connectionCount);
        assertThat(connectionsPage.getAllConnections().size(), is(connectionCount));
    }

    @Then("^she can see unveiled kebab menu of all connections, each of this menu consist of \"(\\w+)\", \"(\\w+)\" and \"(\\w+)\" actions$")
    public void checkAllVisibleKebabMenus(String action1, String action2, String action3) {
        List<String> actions = new ArrayList<>(Arrays.asList(action1, action2, action3));
        for(SelenideElement connection: connectionsPage.getAllConnections()) {
            KebabMenu kebabMenu = new KebabMenu(connection.$(By.xpath(".//button")));

            for(String item: actions) {
                kebabMenu.getItemElement(item).shouldBe(visible);
            }

            kebabMenu.close();
        }
    }

    @Then("^she is presented with help block with text \"([^\"]*)\"$")
    public void helpBlockVisible(String helpText) {
        SelenideElement helpBlock = $(By.className("help-block"));
        helpBlock.shouldBe(visible);
        Assertions.assertThat(helpBlock.getText().equals(helpText)).isTrue();
    }

    @When("^clicks on the \"([^\"]*)\" kebab menu button of \"([^\"]*)\"$")
    public void clickOnKebabMenuButtonOfConnection(String button, String connectionName) throws Throwable {
        KebabMenu kebabMenu = new KebabMenu(connectionsPage.getConnection(connectionName).$(By.xpath(".//button")));
        kebabMenu.open();
        kebabMenu.getItemElement(button).shouldBe(visible).click();
    }
}
