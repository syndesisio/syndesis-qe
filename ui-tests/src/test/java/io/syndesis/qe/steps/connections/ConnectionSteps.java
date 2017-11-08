package io.syndesis.qe.steps.connections;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.common.World;
import io.syndesis.qe.pages.connections.ConnectionConfigurationComponent;
import io.syndesis.qe.pages.connections.ConnectionCreatePage;
import io.syndesis.qe.pages.connections.ConnectionDetailPage;
import io.syndesis.qe.pages.connections.ConnectionsDetailsComponent;
import io.syndesis.qe.pages.connections.ConnectionsListComponent;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionSteps {

	private World world;

	public ConnectionSteps(World w) {
		this.world = w;
	}

	@Then("^Camilla is presented with \"(\\w+)\" connection details")
	public void verifyConnectionDetails(String connectionName) {
		ConnectionDetailPage page = new ConnectionDetailPage();
		assert connectionName.equals(page.connectionName()) : "Connection detail page must show connection name";
	}

	@Then("/^Camilla can see \"(\\w+)\" connection")
	public void expectConnectionTitlePresent(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		SelenideElement connection = listComponent.getConnectionByTitle(connectionName).shouldBe(visible);
	}

	@Then("^Camilla can not see \"(\\w+)\" connection anymore")
	public void expectConnectionTitleNonPresent(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		SelenideElement connection = listComponent.getConnectionByTitle(connectionName).shouldBe(not(exist));
	}

	@Then("^she is presented with a connection create page")
	public void editorOpened() {
		ConnectionCreatePage connPage = new ConnectionCreatePage();
		connPage.getRootElement();
	}

	@When("^Camilla deletes the \"(\\w+)\" connection")
	public void deleteConnection(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.deleteConnection(connectionName);
	}

	@When("^Camilla selects the \"(\\w+)\" connection.*")
	public void selectConnection(String connectionName) {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.goToConnection(connectionName);
	}

	@When("^type \"(\\w+)\" into connection name")
	public void typeConnectionName(String name) {
		ConnectionsDetailsComponent connectionDetails = new ConnectionsDetailsComponent();
		connectionDetails.getInputName().shouldBe(visible).sendKeys(name);
	}

	@When("^type \"(\\w+)\" into connection description")
	public void typeConnectionDescription(String description) {
		ConnectionsDetailsComponent connectionDetails = new ConnectionsDetailsComponent();
		connectionDetails.getDescription().shouldBe(visible).sendKeys(description);
	}

	//TODO: GENERALLY: how to handle exceptions?
	@When("^she fills \"(\\w+)\" connection details")
	public void fillConnectionDetails(String connectionName) throws Exception {
		ConnectionConfigurationComponent connectionConfiguration = new ConnectionConfigurationComponent();
		connectionConfiguration.fillDetails(this.world.getTestConfigConnection(connectionName));
	}

	//TODO: GENERALLY: how to handle exceptions?
	//Kebab menu test, #553 -> part #550.
	@When("^clicks on the kebab menu icon of each available connection")
	public void clickOnAllKebabMenus() throws Exception {
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.clickOnAllKebabButtons();
	}

	@Then("^she is presented with at least \"(\\d+)\" connections")
	public void connectionCount(Integer connectionCount) {
		log.info(String.format("should assert %d", connectionCount));

		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		assert (listComponent.countConnections().equals(connectionCount)) : String.format("There should be %d available", connectionCount);
	}

	//Kebab menu test, #553 -> part #550.
	@Then("^she can see unveiled kebab menu of all connections, each of this menu consist of \"(\\w+)\", \"(\\w+)\" and \"(\\w+)\" actions$")
	public void checkAllVisibleKebabMenus(String action1, String action2, String action3) {
		List<String> actions = new ArrayList<>(Arrays.asList(action1, action2, action3));
		ConnectionsListComponent listComponent = new ConnectionsListComponent();
		listComponent.checkAllKebabElementsAreDisplayed(true, actions);
	}
}
