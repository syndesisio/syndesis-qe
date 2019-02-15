package io.syndesis.qe.steps.apiprovider;

import java.util.List;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.text;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import apicurito.tests.utils.slenide.CommonUtils;
import apicurito.tests.utils.slenide.OperationUtils;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import io.syndesis.qe.pages.integrations.editor.ApiProviderOperationEditorPage;
import io.syndesis.qe.pages.integrations.editor.apiprovider.ApiProviderToolbar;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;
import io.syndesis.qe.pages.integrations.summary.Details;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.openshift.api.model.Route;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ApiProviderWizard;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.NameApiProviderIntegration;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ReviewApiProviderActions;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.UploadApiProviderSpecification;
import io.syndesis.qe.pages.integrations.fragments.OperationsList;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.customizations.connectors.ApicurioSteps;
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

    @When("^select operation flow (.+)$")
    public void selectOperation(String operationName) {
        OperationsList operationsList = new OperationsList(By.cssSelector("syndesis-integration-api-provider-operations-list pfng-list"));
        operationsList.validate();
        operationsList.invokeActionOnItem(operationName, ListAction.CLICK);
    }

    @Then("check ([\\w ]+) operation not present in operation list")
    public void checkOperationNotPresent(String operationName) {
        OperationsList operationsList = new OperationsList(By.cssSelector("syndesis-integration-api-provider-operations-list pfng-list"));
        List<String> operations = operationsList.getOperations();
        assertThat(operations, not(hasItem(operationName)));
    }

    @When("^create api provider spec from ([\\w]+) (.+)$")
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
    public void verifyThereAreErrors(int num) {
        int actual = reviewApiProviderActions.getNumberOfErrors();
        assertEquals("Wrong number of errors", num, actual);
    }

    @Then("^verify there are (\\d+) warnings$")
    public void verifyThereAreWarnings(int num) {
        int actual = reviewApiProviderActions.getNumberOfWarnings();
        assertEquals("Wrong number of warnings", num, actual);
    }

    @Then("^check operation \"([^\"]*)\" implementing \"([^\"]*)\" to \"([^\"]*)\" with status \"([^\"]*)\"$")
    public void checkOperationImplementingWithStatus(String operationName, String operationVerb, String operationPath, String operationStatus) {
        OperationsList operationsList = new OperationsList(By.cssSelector("syndesis-integration-api-provider-operations-list pfng-list"));
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
        Selenide.$(By.xpath("//a[text()='Add a response']")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        Selenide.$(By.cssSelector("#addResponseModal button.btn-primary")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        Selenide.$(By.cssSelector("#api-property-type")).click();
        Selenide.$(By.xpath("//a[text()='String']")).click();
        Selenide.$(By.cssSelector(".response-description .md-description")).click();
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

    @When("^publish API Provider integration$")
    public void publishIntegration() {
        log.info("Publishing integration");
        new ApiProviderToolbar().publish();
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
    public void openAPIProviderOperationSwitcher() throws Throwable {
        new ApiProviderToolbar().goToOperationList();
    }

    @When("^edit API Provider OpenAPI specification$")
    public void editAPIProviderOpenAPISpecification() throws Throwable {
        new ApiProviderToolbar().editOpenApi();
    }

    @When("^go to the ([\\w ]+) operation$")
    public void goToOperation(String operation) throws Throwable {
        new ApiProviderToolbar().goToOperation(operation);
        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }

    @Then("^verify the displayed URL matches regex (.*)$")
    public void verifyTheDisplayedURLMatchesHttpITodoIntegrationApi$(String regex) throws Throwable {
        String apiUrl = new Details().getApiUrl();
        Assertions.assertThat(apiUrl).matches(regex);
    }
}
