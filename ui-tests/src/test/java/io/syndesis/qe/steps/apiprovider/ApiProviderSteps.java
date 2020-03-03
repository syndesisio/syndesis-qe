package io.syndesis.qe.steps.apiprovider;

import static org.junit.Assert.assertEquals;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.endpoints.util.RetryingInvocationBuilder;
import io.syndesis.qe.pages.integrations.editor.apiprovider.ApiProviderToolbar;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ApiProviderWizard;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ReviewApiProviderActions;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.UploadApiProviderSpecification;
import io.syndesis.qe.pages.integrations.fragments.OperationsList;
import io.syndesis.qe.pages.integrations.summary.Details;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.integrations.editor.CreateIntegrationSteps;
import io.syndesis.qe.steps.integrations.editor.EditorSteps;
import io.syndesis.qe.steps.integrations.editor.add.ChooseConnectionSteps;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.TodoUtils;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiProviderSteps {

    private ApiProviderWizard wizard = new ApiProviderWizard();

    private ReviewApiProviderActions reviewApiProviderActions = new ReviewApiProviderActions();
    private ApiProviderToolbar toolbar = new ApiProviderToolbar();
    private OperationsList operationsList = new OperationsList(By.cssSelector(".list-view-pf-view"));
    private Response lastResponse;

    @When("^select API Provider operation flow (.+)$")
    public void selectOperation(String operationName) {
        operationsList.validate();
        operationsList.apiOperationCreateFlow(operationName);
    }

    @Then("check ([\\w ]+) operation is not present in API Provider operation list")
    public void checkOperationNotPresent(String operationName) {
        List<String> operations = operationsList.getOperations();
        assertThat(operations).doesNotContain(operationName);
    }

    @When("^create API Provider spec from ([\\w]+) (.+)$")
    public void createApiProviderSpec(String source, String path) {
        if ("url".equals(source) && "todo-app".equals(path)) {
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

    @Then("^verify API spec warnings contain \"([^\"]*)\"$")
    public void verifyWarningsContain(String warning) {
        assertThat(reviewApiProviderActions.getErrors()).anyMatch(s -> s.contains(warning));
    }

    @Then("^check API Provider operation \"([^\"]*)\" implementing \"([^\"]*)\" to \"([^\"]*)\"$")
    public void checkOperationImplementingWithStatus(String operationName, String operationVerb, String operationPath) {
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
        CommonSteps cs = new CommonSteps();
        cs.clickOnLink("Create Integration");
        new EditorSteps().verifyNewIntegrationEditorOpened();
        new ChooseConnectionSteps().selectConnection("API Provider");
        createApiProviderSpec(source, path);
        navigateToTheNextAPIProviderWizardStep();
        cs.clickOnButton("Next");
        cs.clickOnLink("Save");
        new CreateIntegrationSteps().setIntegrationName(name);
        cs.clickOnButton("Save");
        TestUtils.sleepIgnoreInterrupt(2000);
    }

    @When("^execute ([A-Z]+) on API Provider route ([\\w-]+) endpoint \"([^\"]*)\"$")
    public void executeRequest(String method, String routeName, String endpoint) {
        String url = getUrl(routeName, endpoint);
        lastResponse = getInvocation(url).method(method);
    }

    @When("^execute ([A-Z]+) on API Provider route ([\\w-]+) endpoint \"([^\"]*)\" with body \'([^\']*)\'$")
    public void executeRequest(String method, String routeName, String endpoint, String body) {
        String url = getUrl(routeName, endpoint);
        lastResponse = getInvocation(url).method(method, Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
    }

    @Then("^verify response has body$")
    public void verifyRequestBody(String body) {
        if (lastResponse == null) {
            log.error("Add execute <operation> on API Provider route <route-name>... Before using this step");
            throw new IllegalStateException("A request should be executed before using this step");
        }
        assertEquals(body.trim(), lastResponse.readEntity(String.class));
    }

    @Then("^verify response body contains$")
    public void verifyRequestBodyContains(String bodyPart) {
        if (lastResponse == null) {
            log.error("Add execute <operation> on API Provider route <route-name>... Before using this step");
            throw new IllegalStateException("A request should be executed before using this step");
        }
        assertThat(lastResponse.readEntity(String.class)).contains(bodyPart.trim());
    }

    @Then("^verify response has status (\\d+)$")
    public void verifyRequestStatus(int status) {
        if (lastResponse == null) {
            log.error("Add execute <operation> on API Provider route <route-name>... Before using this step");
            throw new IllegalStateException("A request should be executed before using this step");
        }
        assertEquals(status, lastResponse.getStatus());
    }

    @Then("^verify response has body type \"([^\"]*)\"$")
    public void verifyRequestBodyType(String type) {
        if (lastResponse == null) {
            log.error("Add execute <operation> on API Provider route <route-name>... Before using this step");
            throw new IllegalStateException("A request should be executed before using this step");
        }
        assertEquals(type.trim(), lastResponse.getMediaType().toString());
    }

    private String getUrl(String routeName, String endpoint) {
        Route route = OpenShiftUtils.getInstance().getRoute(routeName);
        String host = "https://" + route.getSpec().getHost();
        String url = host + endpoint;
        return url;
    }

    private Invocation.Builder getInvocation(String url) {
        Client client = RestUtils.getClient();
        return new RetryingInvocationBuilder(client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header("X-Forwarded-User", "pista")
            .header("X-Forwarded-Access-Token", "kral")
            .header("SYNDESIS-XSRF-TOKEN", "awesome"));
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
        assertThat(apiUrl).matches(regex);
    }
}
