package io.syndesis.qe.steps.customizations.connectors.wizard;

import static java.util.Arrays.asList;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.customizations.connectors.wizard.ApiClientConnectorWizard;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.ReviewActions;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.SpecifySecurity;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.UploadSwaggerSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WizardSteps {

    private ApiClientConnectorWizard wizard = new ApiClientConnectorWizard();
    private UploadSwaggerSpecification uploadSwaggerSpecificationWizardPhase = new UploadSwaggerSpecification();
    private ReviewActions reviewActionsWizardPhase = new ReviewActions();
    private SpecifySecurity specifySecurityWizardPhase = new SpecifySecurity();

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
        uploadSwaggerSpecificationWizardPhase.validate();
        List<List<String>> dataRows = fileParams.cells(0);
        List<String> sourceTypes = new ArrayList<String>();
        List<String> urls = new ArrayList<String>();

        for (List<String> row : dataRows) {
            sourceTypes.add(row.get(0));
            urls.add(row.get(1));
        }

        uploadSwaggerSpecificationWizardPhase.upload(sourceTypes.get(0), urls.get(0));
    }

    @Then("(\\w+) sets up security$")
    public void setUpSecurity(String user, DataTable properties) throws Throwable {
        specifySecurityWizardPhase.validate();

        boolean authTypeSet = false;
        Map<String,String> authTypeProperties = new HashMap<>();

        for (List<String> property : properties.raw()) {
            if(property.get(0).equals("authType")) {

                switch (property.get(1)) {
                    case "OAuth 2.0":
                        specifySecurityWizardPhase.selectOauth2();
                        break;
                    case "HTTP Basic Authentication":
                        specifySecurityWizardPhase.selectHttpBasicAuthentication();
                        break;
                    default:
                        Assert.fail("The Auth type < " + property.get(1) + "> is not implemented by the test.");
                }
                authTypeSet = true;

            } else {
                //pick auth properties (with exception of authType) to be filled in the form
                authTypeProperties.put(property.get(0), property.get(1));
            }
        }

        if(!authTypeSet) {
            Assert.fail("Auth type not set.");
        }

        specifySecurityWizardPhase.setUpSecurityProperties(authTypeProperties);
    }

    @Then("(\\w+) sets up the connector details$")
    public void setUpConnectorDetails(String user, DataTable properties) {
        for (List<String> property : properties.raw()) {
            switch (property.get(0)) {
                case "name":
                    setUpConnectorName(user,property.get(1));
                    break;
                case "description":
                    setUpDescription(user,property.get(1));
                    break;
                case "host":
                    setUpDescription(user,property.get(1));
                    break;
                case "baseUrl":
                    setUpDescription(user,property.get(1));
                    break;
                default:
            }
        }
    }

    @Then("^.* sets? up the connector name \"([^\"]*)\"$")
    public void setUpConnectorName(String user, String name) {
        reviewActionsWizardPhase.setConnectorName(name);
    }

    @Then("^.* sets? up description \"([^\"]*)\"$")
    public void setUpDescription(String user, String description) {
        reviewActionsWizardPhase.setDescription(description);
    }

    @Then("^.* sets? up host \"([^\"]*)\"$")
    public void setUpHost(String user, String host) {
        reviewActionsWizardPhase.setHost(host);
    }

    @Then("^.* sets? up base url \"([^\"]*)\"$")
    public void setUpBaseUrl(String user, String baseUrl) {
        reviewActionsWizardPhase.setBaseUrl(baseUrl);
    }
}
