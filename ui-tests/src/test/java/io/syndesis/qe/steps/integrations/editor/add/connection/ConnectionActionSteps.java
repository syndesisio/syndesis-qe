package io.syndesis.qe.steps.integrations.editor.add.connection;

import static com.codeborne.selenide.Condition.visible;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.editor.add.connection.ChooseAction;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.InvokeSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.PeriodicSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionActionSteps {

    private ChooseAction chooseAction = new ChooseAction();
    private ConfigureAction configureAction = new ConfigureAction();
    private PeriodicSql periodicSql = new PeriodicSql();
    private InvokeSql invokeSql = new InvokeSql();

    @When("^she selects \"([^\"]*)\" integration action$")
    public void selectIntegrationAction(String action) {
        if ("Create Opportunity".equals(action)) {
            log.warn("Action {} is not available", action);
            chooseAction.selectAction("Create Salesforce object");
        }
        chooseAction.selectAction(action);
    }

    @Then("^she is presented with an actions list$")
    public void verifyActionsList() {
        log.info("There must be action list loaded");
        chooseAction.getRootElement().shouldBe(visible);
    }

    @Then("^she fills \"(\\w+)\" action configure component input with \"([^\"]*)\" value$")
    public void fillActionConfigureField(String fieldId, String value) {
        log.info("Input should be visible");
        configureAction.fillInput(fieldId, value);
    }

    @Then("^she fills periodic query input with \"([^\"]*)\" value$")
    public void fillPerodicSQLquery(String query) {
        periodicSql.fillSqlInput(query);
    }

    @Then("^she fills period input with \"([^\"]*)\" value$")
    public void fillSQLperiod(String period) {
        periodicSql.fillSQLperiod(period);
    }

    @Then("^she fills invoke query input with \"([^\"]*)\" value$")
    public void fillInvokeSQLquery(String query) {
        invokeSql.fillSqlInput(query);
    }

}
