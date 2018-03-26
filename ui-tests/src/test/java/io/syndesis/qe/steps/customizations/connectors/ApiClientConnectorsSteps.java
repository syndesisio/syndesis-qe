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
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @Then("^(\\w+) creates new API connector$")
    public void createNewApiConnector(String user, DataTable properties) throws Throwable {

        log.info("Connector wizard started");

        /**
         * Upload Swagger Specification wizard phase
         **/
        //| source | file | <path> |
        //| source | url | <path> |
        List<List<String>> sourceDataTable = null;

        /**
         * Specify Security wizard phase
        **/
        //| security | authType | <authType> {HTTP Basic Authentication, OAuth 2.0, ...} |
        //| security | authorizationUrl | <authorizationUrl> |
        //| security | accessTokenUrl | <accessTokenUrl> |
        List<List<String>> securityDataTable = new ArrayList<>();

        /**
         * Review/Edit Connector Details wizard phase
         **/
        //| details | connectorName | <connectorName> |
        String connectorName = null;

        //| details | description | <description> |
        String description = null;

        //| details | hostname | <hostName> |
        //| details | routeHost | <routeName> |
        String host = null;

        //| details | baseUrl | <baseUrl> |
        String baseUrl = null;

        for (List<String> property : properties.raw()) {
            switch (property.get(0)) {
                case "source":
                    switch (property.get(1)) {
                        //| source | file | <path> |
                        case "file":
                        //| source | url | <path> |
                        case "url":
                            log.info("Setting up upload type and path properties of the swagger file");
                            List<String> row = asList(property.get(1), property.get(2));
                            sourceDataTable = new ArrayList<>();
                            sourceDataTable.add(row);
                            break;
                        default:
                    }
                    break;

                case "security":
                    switch (property.get(1)) {
                        //| security | authType | <authType> {HTTP Basic Authentication, OAuth 2.0, ...} |
                        case "authType":
                        //| security | authorizationUrl | <authorizationUrl> |
                        case "authorizationUrl":
                        //| security | accessTokenUrl | <accessTokenUrl> |
                        case "accessTokenUrl":
                            log.info("Setting up access token url property");
                            List<String> row = asList(property.get(1), property.get(2));
                            securityDataTable.add(row);
                            break;
                        default:

                    }
                    break;

                case "details":
                    switch(property.get(1)) {
                        //| details | connectorName | <connectorName> |
                        case "connectorName":
                            log.info("Setting up connector name property");
                            connectorName = property.get(2);
                            break;
                        //| details | description | <description> |
                        case "description":
                            log.info("Setting up description property");
                            description = property.get(2);
                            break;
                        //| details | hostname | <hostName> |
                        case "host":
                            log.info("Setting up hostname property manually");
                            host = property.get(2);
                            break;
                        //| details | routeHost | <routeName> |
                        case "routeHost":
                            log.info("Setting up hostname of the used route property");
                            host = "http://" + OpenShiftUtils.getInstance().getRoute(property.get(2)).getSpec().getHost();
                    }

                default:
            }
        }


        log.info("Validating \"Customizations\" and \"Client Api Connectors\" pages");

        CommonSteps commonSteps = new CommonSteps();
        commonSteps.navigateTo(user, "Customizations");
        commonSteps.validatePage(user, "Customizations");
        commonSteps.clickOnLink("API Client Connectors");
        commonSteps.validatePage(user, "API Client Connectors");

        openNewApiConnectorWizard(user);

        WizardSteps wizardSteps = new WizardSteps();

        if(sourceDataTable==null) {
            Assert.fail("Swagger upload type and path not set");
        }
        log.info("Setting up upload type and path of the swagger file");
        wizardSteps.uploadSwaggerFile(user, DataTable.create(sourceDataTable));

        wizardSteps.navigateToNextWizardStep(user, "Review Actions");
        wizardSteps.navigateToNextWizardStep(user, "SpecifySecurity");

        if(connectorName != null) {
            log.info("Setting up auth type");
            wizardSteps.setUpSecurity(user, DataTable.create(securityDataTable));
            wizardSteps.navigateToNextWizardStep(user, "Review/Edit Connector Details");
        }

        if(connectorName != null) {
            log.info("Setting up connector name");
            wizardSteps.setUpConnectorName(user, connectorName);
        }

        if(description != null) {
            log.info("Setting up connector name");
            wizardSteps.setUpDescription(user, description);
        }

        if(host != null) {
            log.info("Setting up host");
            wizardSteps.setUpHost(user, host);
        }

        if(baseUrl != null) {
            log.info("Setting up base url");
            wizardSteps.setUpBaseUrl(user, baseUrl);
        }

        wizardSteps.finishNewConnectorWizard(user);
        checkNewConnectorIsPresent(user, connectorName);
    }
}
