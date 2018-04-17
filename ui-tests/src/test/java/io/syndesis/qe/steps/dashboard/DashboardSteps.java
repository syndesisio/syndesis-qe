package io.syndesis.qe.steps.dashboard;

import static com.codeborne.selenide.Condition.visible;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.dashboard.DashboardPage;
import io.syndesis.qe.pages.integrations.fragments.IntegrationsList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DashboardSteps {

    @Then("^Integration \"([^\"]*)\" is present in top 5 integrations$")
    public void expectIntegrationPresentinTopFive(String name) {
        log.info("Verifying integration {} is present in top 5 integrations", name);
        DashboardPage dashboardPage = new DashboardPage();
        Assertions.assertThat(dashboardPage.isIntegrationPresent(name));
    }

    @Then("^Camilla can see \"([^\"]*)\" connection on dashboard page$")
    public void expectConnectionTitlePresent(String connectionName) {
        DashboardPage dashboardPage = new DashboardPage();
        SelenideElement connection = dashboardPage.getConnection(connectionName);
        connection.shouldBe(visible);
    }

    @Then("^Camilla can not see \"([^\"]*)\" connection on dashboard page anymore$")
    public void expectConnectionTitleNonPresent(String connectionName) {
        DashboardPage dashboardPage = new DashboardPage();
        SelenideElement connection = dashboardPage.getConnection(connectionName);
        connection.shouldNotBe(visible);
    }

    @When("^Camilla deletes the \"([^\"]*)\" integration in top 5 integrations$")
    public void deleteIntegrationOnDashboard(String integrationName) {
        log.info("Trying to delete {} on top 5 integrations table");
        IntegrationsList integrationsList = new IntegrationsList(By.cssSelector("syndesis-dashboard-integrations"));
        integrationsList.invokeActionOnItem(integrationName, ListAction.DELETE);
    }

    @Then("^Camilla can not see \"([^\"]*)\" integration in top 5 integrations anymore$")
    public void expectIntegrationNotPresentOnDashboard(String name) {
        log.info("Verifying if integration {} is present", name);
        DashboardPage dashboardPage = new DashboardPage();
        Assertions.assertThat(dashboardPage.isIntegrationPresent(name)).isFalse();
    }
}
