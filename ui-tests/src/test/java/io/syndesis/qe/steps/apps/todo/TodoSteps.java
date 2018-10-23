package io.syndesis.qe.steps.apps.todo;

import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import cucumber.api.DataTable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.syndesis.qe.steps.customizations.connectors.ApiClientConnectorsSteps;
import org.assertj.core.api.Assertions;

import com.codeborne.selenide.Selenide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.pages.apps.todo.Todo;
import io.syndesis.qe.utils.OpenShiftUtils;
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
        sleep(seconds*1000);
        Selenide.refresh();

        int endCount = todoPage.getListItemsCount();
        Assertions.assertThat(startCount).isLessThan(endCount);
    }

    //Because app can have another route and url should set dynamically, url is added to the original DataTable
    @Then("create new TODO API connector via URL$")
    public void createNewTodoApiConnector(DataTable properties) throws Throwable {
        if(OpenShiftUtils.getInstance().getRoute("todo2")==null
                || !OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost().equals("/")){
            new TodoSteps().createDefaultRouteForTodo("todo2", "/");
        }
        String host = "http://" + OpenShiftUtils.getInstance().getRoute("todo2").getSpec().getHost();
        String url = host + "/swagger.json";
        List<List<String>> originalTableModifiableCopy = new ArrayList<>(properties.raw());
        List<String> newRow = new ArrayList<>();
        newRow.add("source");
        newRow.add("url");
        newRow.add(url);
        originalTableModifiableCopy.add(newRow);
        DataTable updatedDataTable = properties.toTable(originalTableModifiableCopy);
        new ApiClientConnectorsSteps().createNewApiConnector(updatedDataTable);
    }

    public void createDefaultRouteForTodo(String name, String path){
        final Route route = new RouteBuilder()
                .withNewMetadata()
                .withName(name)
                .endMetadata()
                .withNewSpec()
                .withPath(path)
                .withWildcardPolicy("None")
                .withNewTls()
                .withTermination("edge")
                .withInsecureEdgeTerminationPolicy("Allow")
                .endTls()
                .withNewTo()
                .withKind("Service").withName("todo")
                .endTo()
                .endSpec()
                .build();
        OpenShiftUtils.client().routes().createOrReplace(route);
    }
}
