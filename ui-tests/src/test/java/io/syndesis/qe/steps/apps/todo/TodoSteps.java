package io.syndesis.qe.steps.apps.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.apps.todo.Todo;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.UploadSwaggerSpecification;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TodoUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Selenide;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TodoSteps {
    private Todo todoPage = new Todo();

    @Given("^Set Todo app credentials$")
    public void setCredentials() {
        Account todoAppAccount = new Account();
        todoAppAccount.setService("todo");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("username", "test");
        accountParameters.put("password", "test");
        todoAppAccount.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount("todo", todoAppAccount);
    }

    @Then("^navigate to Todo app$")
    public void openTodoApp() {
        String url = "http://" + OpenShiftUtils.getInstance().getRoute("todo").getSpec().getHost();
        log.info("Opening Todo app on url: " + url);
        getWebDriver().navigate().to(url);
    }

    @Then("^.*checks? Todo list grows in \"([1-9][0-9]*)\" seconds?$")
    public void checkListGrows(int seconds) {
        log.info("Checking Todo app list is growing in " + seconds + " seconds");

        int startCount = todoPage.getListItemsCount();
        sleep(seconds * 1000);
        todoPage.refresh();

        int endCount = todoPage.getListItemsCount();
        assertThat(startCount).isLessThan(endCount);
    }

    @When("^upload TODO API swagger from URL$")
    public void createNewTodoApiConnector() {
        if (OpenShiftUtils.getInstance().getRoute("todo2") == null
            || !OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost().equals("/")) {
            TodoUtils.createDefaultRouteForTodo("todo2", "/");
        }
        String host = "http://" + OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost();
        String url = host + "/swagger.json";
        new UploadSwaggerSpecification().upload("url", url);
    }

    @When("^fill in TODO API host URL$")
    public void fillHostUrl() {
        if (OpenShiftUtils.getInstance().getRoute("todo2") == null
            || !OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost().equals("/")) {
            TodoUtils.createDefaultRouteForTodo("todo2", "/");
        }

        String host = "http://" + OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost();

        Map<String, String> todoConfigMap = new HashMap<>();
        todoConfigMap.put("host", host);

        new Form($(By.id("root"))).fillByTestId(todoConfigMap);
    }

    @Then("^check Todo list has \"(\\w+)\" items")
    public void checkNumberOfItems(int numberOfItems) {
        log.info("Checking Todo app list contains " + numberOfItems + " items/");
        try {
            OpenShiftWaitUtils.waitFor(() -> {
                Selenide.refresh();
                return todoPage.getListItemsCount() == numberOfItems;
            }, 5 * 1000L, 30 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Todo app list did not contain expected number of items in 30 seconds!", e);
        }
    }

    @When("^publish JMS message on Todo app page from resource \"([^\"]*)\"$")
    public void publishMessageFromResourceToDestinationWithName(String resourceName) {
        log.info("Publish JMS message via Todo app");
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL fileUrl = classLoader.getResource("jms_messages/" + resourceName);
        if (fileUrl == null) {
            fail("File with name " + resourceName + " doesn't exist in the resources");
        }

        File file = new File(fileUrl.getFile());
        try {
            todoPage.setJmsForm(new String(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            fail("Error while reading file", e);
        }
        todoPage.sendJmsMessage();
    }

    /**
     * first task e.g. check that "1". task ...
     */
    @Then("^check that \"(\\w+)\". task on Todo app page contains text \"([^\"]*)\"$")
    public void checkNumberValuesExistInTable(int index, String text) {
        String message = todoPage.getMessageFromTodo(index - 1);
        assertThat(message).contains(text);
    }
}
