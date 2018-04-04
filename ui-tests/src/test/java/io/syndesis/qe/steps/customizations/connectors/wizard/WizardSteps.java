package io.syndesis.qe.steps.customizations.connectors.wizard;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.customizations.connectors.wizard.ApiClientConnectorWizard;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.Security;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.UploadSwagger;

public class WizardSteps {

    private ApiClientConnectorWizard wizard = new ApiClientConnectorWizard();
    private UploadSwagger uploadSwaggerWizardStep = new UploadSwagger();
    private Security securityWizardStep = new Security();

    @And("^(\\w+) navigates to the next Api Connector wizard step \"([^\"]*)\"$")
    public void navigateToNextWizardStep(String user, String step) {
        wizard.nextStep();

        //validation ensures that multiple clicking the Next button doesn't click on the same button multiple times before the next step loads and displays
        wizard.getCurrentStep().validate();
    }

    @And("^(\\w+) creates new connector$")
    public void finishNewConnectorWizard(String user) {
        wizard.nextStep();
    }

    @Then("^(\\w+) uploads swagger file$")
    public void uploadSwaggerFile(String user, DataTable fileParams) throws Throwable {
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
    public void setUpSecurityBy(String user, String authType) throws Throwable {
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
}
