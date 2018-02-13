package io.syndesis.qe.steps.customizations.connectors;

import static java.util.Arrays.asList;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.customizations.connectors.ApiClientConnectors;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.customizations.connectors.wizard.WizardSteps;

public class ApiClientConnectorsSteps {

	private static ApiClientConnectors apiClientConnectorsPage = new ApiClientConnectors();

	@Then("^(\\w+) opens new Api Connector wizard$")
	public void openNewApiConnectorWizard(String user) throws Throwable {
		apiClientConnectorsPage.startWizard();
	}

	@Then("^(\\w+) opens the API connector \"([^\"]*)\" detail$")
	public void openApiConnectorDetail(String userName, String connectorName) {
		apiClientConnectorsPage.clickConnectorByTitle(connectorName);
	}

	@Then("(\\w+) is presented with the new connector \"([^\"]*)\"$")
	public void checkNewConnectorIsPresent(String user, String connectorName) throws Throwable {
		Assert.assertTrue("Connector [" + connectorName + "] is not listed.", apiClientConnectorsPage.isConnectorPresent(connectorName));
	}

	@Then("^(\\w+) is presented with a connectors list of size (\\d+)$")
	public void checkConnectorsListSize(String userName, String listSize) {
		Assert.assertTrue("The connectors list should be of size <" + listSize + ">.", apiClientConnectorsPage.isConnectorsListLongAs(Integer.parseInt(listSize)));
	}

	//***************************************************************************
	//******************************* bulk steps ********************************
	//***************************************************************************

	@Then("^(\\w+) creates new API connector \"([^\"]*)\"$")
	public void createNewApiConnector(String user, String connectorName, DataTable properties) throws Throwable {

		String securityAuthType = null;

		for (List<String> property : properties.raw()) {
			switch (property.get(0)) {
				case "security":
					switch (property.get(1)) {
						//| security | authType | OAuth 2.0 |
						case "authType":
							securityAuthType = property.get(2);
							break;
						default:
					}
					break;

				default:
			}
		}

		CommonSteps commonSteps = new CommonSteps();
		commonSteps.navigateTo(user, "Customizations");
		commonSteps.validatePage(user, "Customizations");
		commonSteps.clickOnLink("API Client Connectors");
		commonSteps.validatePage(user, "API Client Connectors");

		openNewApiConnectorWizard(user);

		WizardSteps wizardSteps = new WizardSteps();

		List<String> row = asList("file", "swagger_files/api_client_connector/petstore_swagger.json");
		List<List<String>> dataTable = new ArrayList<>();
		dataTable.add(row);
		wizardSteps.uploadSwaggerFile(user, DataTable.create(dataTable));

		wizardSteps.navigateToNextWizardStep(user, "Review Swagger Action");
		wizardSteps.navigateToNextWizardStep(user, "Security");

		wizardSteps.setUpSecurityBy(user, securityAuthType);
		wizardSteps.navigateToNextWizardStep(user, "General Connector Info");
		wizardSteps.finishNewConnectorWizard(user);
		checkNewConnectorIsPresent(user, connectorName);
	}
}
