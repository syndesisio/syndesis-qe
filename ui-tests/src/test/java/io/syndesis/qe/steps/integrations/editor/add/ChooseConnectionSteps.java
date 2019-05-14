package io.syndesis.qe.steps.integrations.editor.add;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.connections.fragments.list.ConnectionsList;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseConnectionSteps {

    private ConnectionsList connectionsList = new ConnectionsList(By.cssSelector(".container-cards-pf.container-fluid"));
    private IntegrationFlowView integrationFlowView = new IntegrationFlowView();

    /**
     * whether it's start or finish connection
     *
     * @param position
     */
    @Then("^check that position of connection to fill is \"([^\"]*)\"$")
    public void verifyTypeOfConnection(String position) {
        log.info("{} connection must be active", position);
        assertTrue("There was no active icon found for position " + position, integrationFlowView.verifyActivePosition(position));
    }

    @When("^.*selects? the \"([^\"]*)\" connection$")
    public void selectConnection(String connectionName) {
        connectionsList.invokeActionOnItem(connectionName, ListAction.CLICK);
    }


    @Then("^check that connections list does not contain \"([^\"]*)\" connection$")
    public void checkThatConnectionsListDoesNotContainConnection(String connection) throws Throwable {
        assertFalse("Did not expect to find connection " + connection, connectionsList.getItem(connection).isDisplayed());
    }

    @Then("^check that connection \"([^\"]*)\" is marked as Tech Preview$")
    public void checkThatConnectionIsMarkedAsTechPreview(String connection) throws Throwable {
        assertTrue(connection + " should be marked as tech preview", connectionsList.isConnectionTechPreview(connection));
    }
}
