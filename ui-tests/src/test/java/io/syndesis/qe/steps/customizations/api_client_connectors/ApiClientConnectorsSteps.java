package io.syndesis.qe.steps.customizations.api_client_connectors;

import static java.util.Arrays.asList;

import org.junit.Assert;

import com.codeborne.selenide.SelenideElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.customizations.api_client_connectors.detail.ApiClientConnectorDetail;
import io.syndesis.qe.pages.customizations.api_client_connectors.wizard.ApiClientConnectorWizard;
import io.syndesis.qe.pages.customizations.api_client_connectors.wizard.GeneralConnectorInfo;
import io.syndesis.qe.pages.customizations.api_client_connectors.wizard.ReviewSwaggerActions;
import io.syndesis.qe.pages.customizations.api_client_connectors.wizard.Security;
import io.syndesis.qe.pages.customizations.api_client_connectors.wizard.UploadSwagger;
import io.syndesis.qe.pages.customizations.api_client_connectors.ApiClientConnectors;
import io.syndesis.qe.steps.CommonSteps;

public class ApiClientConnectorsSteps {

	private Object customizations = new Object();
	private ApiClientConnectors apiClientConnectorsPage = new ApiClientConnectors();
	private ApiClientConnectorWizard wizard;
	private UploadSwagger uploadSwaggerWizardStep = new UploadSwagger();
	private ReviewSwaggerActions reviewSwaggerActionsWizardStep = new ReviewSwaggerActions();
	private Security securityWizardStep = new Security();
	private GeneralConnectorInfo generalConnectorInfoWizardStep = new GeneralConnectorInfo();
	private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

	@Given("^(\\w+) opens \"([^\"]*)\" page$")
	public void she_opens_api_client_connector_page(String user, String pageName) {
		customizations.equals("open API Client Connectors page");
		apiClientConnectorsPage.validate();
	}

	@And("^(\\w+) navigates to the next Api Connector wizard step \"([^\"]*)\"$")
	public void she_navigates_to_the_next_Api_Connector_wizard_step(String user, String step) {
		wizard.nextStep();

		//validation ensures that multiple clicking the Next button doesn't click on the same button multiple times before the next step loads and displays
		wizard.getCurrentStep().validate();
	}

	@And("^(\\w+) creates new connector$")
	public void she_creates_new_connector(String user) {
		wizard.nextStep();
	}

	@Then("^(\\w+) opens new Api Connector wizard$")
	public void she_opens_new_api_connector_wizard(String user) throws Throwable {
		apiClientConnectorsPage.startWizard();
		wizard = new ApiClientConnectorWizard();
	}

	@Then("^(\\w+) uploads swagger file$")
	public void she_uploads_swagger_file(String user, DataTable fileParams) throws Throwable {
		uploadSwaggerWizardStep.validate();
		List<List<String>> dataRows = fileParams.cells(0);
		List<String> sourceTypes = new ArrayList<String>();
		List<String> urls = new ArrayList<String>();

		for (List<String> row : dataRows) {
			sourceTypes.add(row.get(0));
			urls.add(row.get(1));
		}

		uploadSwaggerWizardStep.upload(sourceTypes.get(0), urls.get(0));
	}

	@Then("^(\\w+) reviews swagger actions$")
	public void she_reviews_swagger_actions(String user) throws Throwable {
		reviewSwaggerActionsWizardStep.validate();
		reviewSwaggerActionsWizardStep.review();
		reviewSwaggerActionsWizardStep.nextWizardStep();
	}

	@Then("(\\w+) sets up security by \"([^\"]*)\"$")
	public void she_sets_up_security_by(String user, String authType) throws Throwable {
		securityWizardStep.validate();
		//String authType, String authenticationUrl, String authorizationUrl, String scopes
		switch (authType) {
			case "OAuth 2.0":
				securityWizardStep.setUpSecurity(TestConfiguration.syndesisUrl() + TestConfiguration.syndesisCallbackUrlSuffix());
				break;
			default:
				throw new UnsupportedOperationException("The Auth type < " + authType + "> is not implemented by the test.");
		}
	}

	@Then("^(\\w+) reviews general info$")
	public void she_reviews_general_info(String user) throws Throwable {
		generalConnectorInfoWizardStep.validate();
		generalConnectorInfoWizardStep.review();
		generalConnectorInfoWizardStep.validate();
	}

	@Then("(\\w+) is presented with the new connector \"([^\"]*)\"$")
	public void she_is_presented_with_the_new_connector(String user, String connectorName) throws Throwable {
		Assert.assertTrue("Connector [" + connectorName + "] is not listed.", apiClientConnectorsPage.isConnectorPresent(connectorName));
	}

	@Then("^(\\w+) creates new API connector \"([^\"]*)\"$")
	public void she_creates_new_api_connector(String user, String connectorName, DataTable properties) throws Throwable {

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
		she_opens_new_api_connector_wizard(user);

		List<String> row = asList("file", "swagger_files/api_client_connector/petstore_swagger.json");
		List<List<String>> dataTable = new ArrayList<>();
		dataTable.add(row);

		she_uploads_swagger_file(user, DataTable.create(dataTable));
		she_navigates_to_the_next_Api_Connector_wizard_step(user, "Review Swagger Action");
		she_navigates_to_the_next_Api_Connector_wizard_step(user, "Security");

		List<String> secRow = asList("authType", "authenticationUrl", "https://syndesis.192.168.42.188.xip.io/api/v1/credentials/callback", "scopes");
		List<List<String>> secDataTable = new ArrayList<>();
		secDataTable.add(secRow);

		she_sets_up_security_by(user, securityAuthType);
		she_navigates_to_the_next_Api_Connector_wizard_step(user, "General Connector Info");
		she_creates_new_connector(user);
		she_is_presented_with_the_new_connector(user, connectorName);
	}

	@Then("^(\\w+) is presented with a connectors list of size (\\d+)$")
	public void she_is_presented_with_an_connectors_list_of_size(String userName, String listSize) {
		Assert.assertTrue("The connectors list should be of size <" + listSize + ">.", apiClientConnectorsPage.isConnectorsListLongAs(Integer.parseInt(listSize)));
	}

	@Then("^(\\w+) opens the API connector \"([^\"]*)\" detail$")
	public void she_opens_the_api_connector_detail(String userName, String connectorName) {
		apiClientConnectorsPage.clickConnectorByTitle(connectorName);
	}

	@Then("^(\\w+) edits property")
	public void she_edits_property(String userName, DataTable dataTable) {
		for (List<String> data : dataTable.raw()) {
			String propertyName = data.get(0);
			String propertyValue = data.get(1);
			String id = data.get(2);

			connectorDetailPage.getTextToEditElement(propertyName).click();
			connectorDetailPage.getTextEditor(id).setValue(propertyValue);
			connectorDetailPage.getEditablePropertyLabel(propertyName).click();
			//check value has been set
			connectorDetailPage.getTextToEditElement(propertyName).text().equals(propertyValue);
		}
	}
}