package io.syndesis.qe.steps.customizations.connectors;

import static java.util.Arrays.asList;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import io.syndesis.qe.pages.customizations.connectors.wizard.ApiClientConnectorWizard;
import io.syndesis.qe.pages.customizations.connectors.wizard.GeneralConnectorInfo;
import io.syndesis.qe.pages.customizations.connectors.wizard.ReviewSwaggerActions;
import io.syndesis.qe.pages.customizations.connectors.wizard.Security;
import io.syndesis.qe.pages.customizations.connectors.wizard.UploadSwagger;
import io.syndesis.qe.pages.customizations.connectors.ApiClientConnectors;
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
    public void sheOpensApiClientConnectorPage(String user, String pageName) {
        customizations.equals("open API Client Connectors page");
        apiClientConnectorsPage.validate();
    }

    @And("^(\\w+) navigates to the next Api Connector wizard step \"([^\"]*)\"$")
    public void sheNavigatesToTheNextApiConnectorWizardStep(String user, String step) {
        wizard.nextStep();

        //validation ensures that multiple clicking the Next button doesn't click on the same button multiple times before the next step loads and
        // displays
        wizard.getCurrentStep().validate();
    }

    @And("^(\\w+) creates new connector$")
    public void sheCreatesNewConnector(String user) {
        wizard.nextStep();
    }

    @Then("^(\\w+) opens new Api Connector wizard$")
    public void sheOpensNewApiConnectorWizard(String user) throws Throwable {
        apiClientConnectorsPage.startWizard();
        wizard = new ApiClientConnectorWizard();
    }

    @Then("^(\\w+) uploads swagger file$")
    public void sheUploadsSwaggerFile(String user, DataTable fileParams) throws Throwable {
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

    @Then("(\\w+) sets up security by \"([^\"]*)\"$")
    public void sheSetsUpSecurityBy(String user, String authType) throws Throwable {
        securityWizardStep.validate();
        //String authType, String authenticationUrl, String authorizationUrl, String scopes
        switch (authType) {
            case "OAuth 2.0":
                securityWizardStep.setUpOAuth2Security(TestConfiguration.syndesisUrl() + TestConfiguration.syndesisCallbackUrlSuffix());
                break;
            default:
                Assert.fail("The Auth type < " + authType + "> is not implemented by the test.");
        }
    }

    @Then("(\\w+) is presented with the new connector \"([^\"]*)\"$")
    public void sheIsPresentedWithTheNewConnector(String user, String connectorName) throws Throwable {
        Assert.assertTrue("Connector [" + connectorName + "] is not listed.", apiClientConnectorsPage.isConnectorPresent(connectorName));
    }

    @Then("^(\\w+) creates new API connector \"([^\"]*)\"$")
    public void sheCreatesNewApiConnector(String user, String connectorName, DataTable properties) throws Throwable {

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
        sheOpensNewApiConnectorWizard(user);

        List<String> row = asList("file", "swagger_files/api_client_connector/petstore_swagger.json");
        List<List<String>> dataTable = new ArrayList<>();
        dataTable.add(row);

        sheUploadsSwaggerFile(user, DataTable.create(dataTable));
        sheNavigatesToTheNextApiConnectorWizardStep(user, "Review Swagger Action");
        sheNavigatesToTheNextApiConnectorWizardStep(user, "Security");

        sheSetsUpSecurityBy(user, securityAuthType);
        sheNavigatesToTheNextApiConnectorWizardStep(user, "General Connector Info");
        sheCreatesNewConnector(user);
        sheIsPresentedWithTheNewConnector(user, connectorName);
    }

    @Then("^(\\w+) is presented with a connectors list of size (\\d+)$")
    public void sheIsPresentedWithAnConnectorsListOfSize(String userName, String listSize) {
        Assert.assertTrue("The connectors list should be of size <" + listSize + ">.",
                apiClientConnectorsPage.isConnectorsListLongAs(Integer.parseInt(listSize)));
    }

    @Then("^(\\w+) opens the API connector \"([^\"]*)\" detail$")
    public void sheOpensTheApiConnectorDetail(String userName, String connectorName) {
        apiClientConnectorsPage.clickConnectorByTitle(connectorName);
    }

    @Then("^(\\w+) edits property")
    public void sheEditsProperty(String userName, DataTable dataTable) {
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
