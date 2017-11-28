package io.syndesis.qe.steps.connections;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.pages.connections.detail.ConnectionDetailPage;
import io.syndesis.qe.pages.connections.edit.ConnectionCreatePage;
import io.syndesis.qe.pages.connections.list.ConnectionListPage;
import io.syndesis.qe.pages.connections.list.ConnectionsListComponent;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionSteps {

	private ConnectionCreatePage editPage = new ConnectionCreatePage();
	private ConnectionDetailPage detailPage = new ConnectionDetailPage();
	private ConnectionListPage listPage = new ConnectionListPage();

	@Then("^Camilla is presented with \"([^\"]*)\" connection details")
	public void verifyConnectionDetails(String connectionName) {
		log.info("Connection detail page must show connection name");
		assertThat(detailPage.connectionName(), is(connectionName));
	}

	@Then("^Camilla can see \"(\\w+)\" connection$")
	public void expectConnectionTitlePresent(String connectionName) {
		listPage.getListComponent().getConnectionByTitle(connectionName).shouldBe(visible);
	}

	@Then("^Camilla can not see \"([^\"]*)\" connection anymore$")
	public void expectConnectionTitleNonPresent(String connectionName) {
		listPage.getListComponent().getConnectionByTitle(connectionName).shouldBe(not(exist));
	}

	@Then("^she is presented with a connection create page$")
	public void editorOpened() {
		ConnectionCreatePage connPage = new ConnectionCreatePage();
		connPage.getRootElement();
	}

	@When("^Camilla deletes the \"([^\"]*)\" connection$")
	public void deleteConnection(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.deleteConnection(connectionName);
	}

	@When("^Camilla selects the \"([^\"]*)\" connection$")
	public void selectConnection(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.goToConnection(connectionName);
	}

	@When("^types? \"([^\"]*)\" into connection name$")
	public void typeConnectionName(String name) {
		editPage.getConnectionDetails().getInputName().shouldBe(visible).sendKeys(name);
	}

	@When("^types? \"([^\"]*)\" into connection description$")
	public void typeConnectionDescription(String description) {
		editPage.getConnectionDetails().getDescription().shouldBe(visible).sendKeys(description);
	}

	@When("^she fills \"([^\"]*)\" connection details$")
	public void fillConnectionDetails(String connectionName) {
		Optional<Account> optional = new AccountsDirectory().getAccount(connectionName);
		if (optional.isPresent()) {
			editPage.getConnectionConfiguration().fillDetails(optional.get().getProperties());
		} else {
			String nameTransformed = connectionName.toLowerCase().replaceAll(" ", "_");
			new AccountsDirectory().getAccount(nameTransformed).ifPresent(account -> editPage.getConnectionConfiguration().fillDetails(account.getProperties()));
		}
	}

	@When("^clicks? on the kebab menu icon of each available connection$")
	public void clickOnAllKebabMenus() {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.clickOnAllKebabButtons();
	}

	@Then("^she is presented with at least \"(\\d+)\" connections$")
	public void connectionCount(Integer connectionCount) {
		log.info("There should be {} available", connectionCount);
		assertThat(listPage.getListComponent().countConnections(), is(connectionCount));
	}

	@Then("^she can see unveiled kebab menu of all connections, each of this menu consist of \"(\\w+)\", \"(\\w+)\" and \"(\\w+)\" actions$")
	public void checkAllVisibleKebabMenus(String action1, String action2, String action3) {
		List<String> actions = new ArrayList<>(Arrays.asList(action1, action2, action3));
		listPage.getListComponent().checkAllKebabElementsAreDisplayed(true, actions);
	}

	@Then("^she is presented with help block with text \"([^\"]*)\"$")
	public void helpBlockVisible(String helpText) {
		SelenideElement helpBlock = $(By.className("help-block"));
		helpBlock.shouldBe(visible);
		Assertions.assertThat(helpBlock.getText().equals(helpText)).isTrue();
	}

	@When("^clicks on the \"([^\"]*)\" kebab menu button of \"([^\"]*)\"$")
	public void clickOnKebabMenuButtonOfConnection(String button, String connectionName) throws Throwable {
		ConnectionsListComponent conn = new ConnectionsListComponent();
		conn.clickKebabMenuButton(button, connectionName);
	}
}
