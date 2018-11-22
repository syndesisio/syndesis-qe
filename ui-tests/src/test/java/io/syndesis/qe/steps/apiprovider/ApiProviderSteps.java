package io.syndesis.qe.steps.apiprovider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codeborne.selenide.Selenide;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.openshift.api.model.Route;
import io.syndesis.qe.endpoints.AbstractEndpoint;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ApiProviderWizard;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.NameApiProviderIntegration;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ReviewApiProviderActions;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.UploadApiProviderSpecification;
import io.syndesis.qe.pages.integrations.fragments.OperationsList;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.apps.todo.TodoSteps;
import io.syndesis.qe.steps.customizations.connectors.ApicurioSteps;
import io.syndesis.qe.steps.integrations.editor.add.ChooseConnectionSteps;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import static org.junit.Assert.assertEquals;

@Slf4j
public class ApiProviderSteps {

    private ApiProviderWizard wizard = new ApiProviderWizard();

    private ReviewApiProviderActions reviewApiProviderActions = new ReviewApiProviderActions();
    private final OperationsList operationsList = new OperationsList(By.className("operations-list"));

    @When("^select operation (.+)$")
    public void selectOperation(String operationName) {
        OperationsList operationsList = new OperationsList(By.className("operations-list"));
        operationsList.validate();
        operationsList.invokeActionOnItem(operationName, ListAction.CLICK);
    }

    @When("^create api provider spec from ([\\w]+) (.+)$")
    public void createApiProviderSpec(String source, String path) {
        if ("url".equals(source)) {
            new TodoSteps().createDefaultRouteForTodo(path, "/");
            path = "http://" + OpenShiftUtils.getInstance().getRoute(path).getSpec().getHost() + "/swagger.json";
        }
        new UploadApiProviderSpecification().upload(source, path);
    }

    @When("^navigate to the next API Provider wizard step$")
    public void navigateToTheNextAPIProviderWizardStep() {
        wizard.nextStep();
        wizard.getCurrentStep().validate();
    }

    @When("^fill in integration name \"([^\"]*)\"$")
    public void fillInIntegrationName(String integrationName) {
        new NameApiProviderIntegration().setName(integrationName);
    }

    @When("^finish API Provider wizard$")
    public void finishAPIProviderWizard() {
        wizard.nextStep();
    }

    @Then("^verify there are (\\d+) operations defined$")
    public void verifyThereAreOperationsDefined(int num) {
        int actual = reviewApiProviderActions.getNumberOfOperations();
        assertEquals("Wrong number of operations", num, actual);
    }

    @Then("^verify (\\d+) operations are tagged (\\w+)$")
    public void verifyOperationsIsTaggedUpdating(int num, String tag) {
        int actual = reviewApiProviderActions.getNumberOfOperationsByTag(tag);
        assertEquals("Wrong number of operations", num, actual);
    }

    @Then("^verify there are (\\d+) errors$")
    public void verifyThereAreErrors(int num)  {
        int actual = reviewApiProviderActions.getNumberOfErrors();
        assertEquals("Wrong number of errors", num, actual);
    }

    @Then("^verify there are (\\d+) warnings$")
    public void verifyThereAreWarnings(int num) {
        int actual = reviewApiProviderActions.getNumberOfWarnings();
        assertEquals("Wrong number of warnings", num, actual);
    }

    @Then("^check operation \"([^\"]*)\" implementing \"([^\"]*)\" with status \"([^\"]*)\"$")
    public void checkOperationImplementingWithStatus(String operationName, String operationPath, String operationStatus) {
        operationsList.validate();
        String description = operationsList.getDescription(operationName);
        String status = operationsList.getStatus(operationName);

        assertEquals("Wrong operation path for operation " + operationName, operationPath, description);
        assertEquals("Wrong operation status for operation " + operationName, operationStatus, status);
    }

    /**
     * Convenience method to prevent repeating the same steps over and over again
     * @param name
     * @param source
     * @param path
     */
    @When("^create an API Provider integration \"([^\"]*)\" from (\\w+) (.+)$")
    public void createTheTODOIntegration(String name, String source, String path) {
        new CommonSteps().clickOnButton("Create Integration");
        new ChooseConnectionSteps().selectConnection("API Provider");
        createApiProviderSpec(source, path);
        navigateToTheNextAPIProviderWizardStep();
        navigateToTheNextAPIProviderWizardStep();
        fillInIntegrationName(name);
        finishAPIProviderWizard();
    }

    @When("^create an API Provider operation in apicurio$")
    public void createAnAPIProviderOperationInApicurio() {
        // TODO: temporary solution until apicurio support is added to the test suite
        new ApicurioSteps().addOperation();
        Selenide.$(By.cssSelector("#operations-section-body div.type")).click();
        Selenide.$(By.xpath("//a[text()='Add a response']")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        Selenide.$(By.cssSelector("#addResponseModal button.btn-primary")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        Selenide.$(By.cssSelector("schema-type")).click();
        Selenide.$(By.cssSelector("#api-response-type")).click();
        Selenide.$(By.xpath("//a[text()='String']")).click();
        Selenide.$(By.cssSelector("response-row div.description")).click();
        Selenide.$(By.cssSelector("div.response-description")).click();
        // wtf, this should not be necessary, but it is
        Selenide.$(By.cssSelector("ace-editor textarea")).sendKeys("desc");
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        Selenide.$(By.xpath("//button[@title='Save changes.']")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }


    @Then("^verify that executing ([A-Z]+) on route ([\\w-]+) endpoint \"([^\"]*)\" returns status (\\d+) and body$")
    public void verifyThatEndpointReturnsStatusAndBody(String method, String routeName, String endpoint, int status, String body) {
        String url = getUrl(routeName, endpoint);
        Response response = getInvocation(url).method(method);
        checkResponse(response, status, body);
    }

    @Then("^verify that executing ([A-Z]+) on route ([\\w-]+) endpoint \"([^\"]*)\" with request '(.+)' returns status (\\d+) and body$")
    public void verifyThatEndpointReturnsStatusAndBody(String method, String routeName,
                                                       String endpoint, String requestBody, int status, String body) {
        String url = getUrl(routeName, endpoint);
        Response response = getInvocation(url).method(method, Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE));
        checkResponse(response, status, body);
    }

    private void checkResponse(Response response, int status, String body) {
        assertEquals("Wrong status", status, response.getStatus());
        String responseBody = response.readEntity(String.class);
        assertEquals("Wrong body", body, responseBody);

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
}
