package io.syndesis.qe.steps.apiprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.openshift.api.model.Route;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.integrations.editor.apiprovider.ApiProviderToolbar;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ApiProviderWizard;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.NameApiProviderIntegration;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ReviewApiProviderActions;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.UploadApiProviderSpecification;
import io.syndesis.qe.pages.integrations.fragments.OperationsList;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.integrations.editor.EditorSteps;
import io.syndesis.qe.steps.integrations.editor.add.ChooseConnectionSteps;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.TodoUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiProviderSteps {

    private ApiProviderWizard wizard = new ApiProviderWizard();

    private ReviewApiProviderActions reviewApiProviderActions = new ReviewApiProviderActions();
    private ApiProviderToolbar toolbar = new ApiProviderToolbar();
    private OperationsList operationsList = new OperationsList(By.cssSelector("syndesis-integration-api-provider-operations-list pfng-list"));

    @When("^select API Provider operation flow (.+)$")
    public void selectOperation(String operationName) {
        operationsList.validate();
        operationsList.apiOperationCreateFlow(operationName);
    }

    @Then("check ([\\w ]+) operation is not present in API Provider operation list")
    public void checkOperationNotPresent(String operationName) {
        List<String> operations = operationsList.getOperations();
        assertThat(operations, not(hasItem(operationName)));
    }

    @When("^create API Provider spec from ([\\w]+) (.+)$")
    public void createApiProviderSpec(String source, String path) {
        if ("url".equals(source)) {
            TodoUtils.createDefaultRouteForTodo(path, "/");
            path = "http://" + OpenShiftUtils.getInstance().getRoute(path).getSpec().getHost() + "/swagger.json";
        }
        new UploadApiProviderSpecification().upload(source, path);
    }

    @When("^navigate to the next API Provider wizard step$")
    public void navigateToTheNextAPIProviderWizardStep() {
        wizard.nextStep();
        wizard.getCurrentStep().validate();
    }

    @When("^fill in API Provider integration name \"([^\"]*)\"$")
    public void fillInIntegrationName(String integrationName) {
        new NameApiProviderIntegration().setName(integrationName);
    }

    @When("^finish API Provider wizard$")
    public void finishAPIProviderWizard() {
        wizard.nextStep();
    }

    @Then("^verify there are (\\d+) API Provider operations defined$")
    public void verifyThereAreOperationsDefined(int num) {
        int actual = reviewApiProviderActions.getNumberOfOperations();
        assertEquals("Wrong number of operations", num, actual);
    }

    @Then("^verify (\\d+) API Provider operations are tagged (\\w+)$")
    public void verifyOperationsIsTaggedUpdating(int num, String tag) {
        int actual = reviewApiProviderActions.getNumberOfOperationsByTag(tag);
        assertEquals("Wrong number of operations", num, actual);
    }

    @Then("^verify there are (\\d+) errors for API Provider operations$")
    public void verifyThereAreErrors(int num) {
        int actual = reviewApiProviderActions.getNumberOfErrors();
        assertEquals("Wrong number of errors", num, actual);
    }

    @Then("^verify there are (\\d+) warnings for API Provider operations$")
    public void verifyThereAreWarnings(int num) {
        int actual = reviewApiProviderActions.getNumberOfWarnings();
        assertEquals("Wrong number of warnings", num, actual);
    }

    @Then("^check API Provider operation \"([^\"]*)\" implementing \"([^\"]*)\" to \"([^\"]*)\" with status \"([^\"]*)\"$")
    public void checkOperationImplementingWithStatus(String operationName, String operationVerb, String operationPath, String operationStatus) {
        operationsList.validate();

        String verb = operationsList.getVerb(operationName);
        String path = operationsList.getUrl(operationName);

        assertEquals("Wrong verb path for operation " + operationName, operationVerb, verb);
        assertEquals("Wrong operation path for operation " + operationName, operationPath, path);
    }

    /**
     * Convenience method to prevent repeating the same steps over and over again
     *
     * @param name
     * @param source
     * @param path
     */
    @When("^create an API Provider integration \"([^\"]*)\" from (\\w+) (.+)$")
    public void createTheTODOIntegration(String name, String source, String path) {
        new CommonSteps().clickOnButton("Create Integration");
        new EditorSteps().verifyNewIntegrationEditorOpened();
        new ChooseConnectionSteps().selectConnection("API Provider");
        createApiProviderSpec(source, path);
        navigateToTheNextAPIProviderWizardStep();
        navigateToTheNextAPIProviderWizardStep();
        fillInIntegrationName(name);
        finishAPIProviderWizard();
    }

    @Then("^verify that executing ([A-Z]+) on API Provider route ([\\w-]+) endpoint \"([^\"]*)\" returns status (\\d+) and body$")
    public void verifyThatEndpointReturnsStatusAndBody(String method, String routeName, String endpoint, int status, String body) {
        String url = getUrl(routeName, endpoint);
        Response response = getInvocation(url).method(method);
        checkResponse(response, status, body);
    }

    @Then("^verify that executing ([A-Z]+) on API Provider route ([\\w-]+) endpoint \"([^\"]*)\" with request '(.+)' returns status (\\d+) and body$")
    public void verifyThatEndpointReturnsStatusAndBody(String method, String routeName,
            String endpoint, String requestBody, int status, String body) {
        String url = getUrl(routeName, endpoint);
        Response response = getInvocation(url).method(method, Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE));
        checkResponse(response, status, body);
    }

    @When("^publish API Provider integration$")
    public void publishIntegration() {
        log.info("Publishing integration");
        toolbar.publish();
    }

    private void checkResponse(Response response, int status, String body) {
        SoftAssertions sa = new SoftAssertions();
        sa.assertThat(response.getStatus()).isEqualTo(status).describedAs("Wrong status");
        String responseBody = response.readEntity(String.class);
        sa.assertThat(responseBody).isEqualTo(body).describedAs("Wrong body");
        sa.assertAll();
    }

    private String getUrl(String routeName, String endpoint) {
        Route route = OpenShiftUtils.getInstance().getRoute(routeName);
        String host = "https://" + route.getSpec().getHost();
        String url = host + endpoint;
        return url;
    }

    private Invocation.Builder getInvocation(String url) {
        Client client = RestUtils.getClient();
        Invocation.Builder invocation = client
                .target(url)
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral")
                .header("SYNDESIS-XSRF-TOKEN", "awesome");
        return invocation;
    }

    @When("^go to API Provider operation list$")
    public void openAPIProviderOperationSwitcher() {
        toolbar.goToOperationList();
    }

    @When("^edit API Provider OpenAPI specification$")
    public void editAPIProviderOpenAPISpecification() {
        toolbar.editOpenApi();
    }

    @When("^go to the ([\\w ]+) API Provider operation$")
    public void goToOperation(String operation) {
        toolbar.goToOperation(operation);
        TestUtils.sleepForJenkinsDelayIfHigher(1);
    }

    @Then("^verify the displayed API Provider URL matches regex (.*)$")
    public void verifyTheDisplayedURLMatchesHttpITodoIntegrationApi$(String regex) {
        String apiUrl = new Details().getApiUrl();
        Assertions.assertThat(apiUrl).matches(regex);
    }
}
