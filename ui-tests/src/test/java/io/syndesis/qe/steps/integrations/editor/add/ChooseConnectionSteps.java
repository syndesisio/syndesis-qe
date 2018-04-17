package io.syndesis.qe.steps.integrations.editor.add;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.connections.fragments.list.ConnectionsList;
import io.syndesis.qe.pages.integrations.editor.AddToIntegration;
import io.syndesis.qe.pages.integrations.editor.Editor;
import io.syndesis.qe.pages.integrations.editor.add.ChooseStep;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.AbstractStep;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.StepFactory;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseConnectionSteps {

    private ConnectionsList connectionsList = new ConnectionsList(By.xpath("//syndesis-connections-list"));
    private IntegrationFlowView integrationFlowView = new IntegrationFlowView();
    private AddToIntegration addToIntegration = new AddToIntegration();
    private ChooseStep chooseStep = new ChooseStep();
    private StepFactory stepFactory = new StepFactory();
    private Editor editor = new Editor();

    @Then("^she adds \"(\\d+)\" random steps and then checks the structure$")
    public void addRandomStepsAndCheckRest(Integer numberOfSteps) {
        log.info("Adding random phases");
        List<String> list = integrationFlowView.getStepsArray();
        addToIntegration.getButton("Add a Step").shouldBe(visible).click();
        ElementsCollection links = addToIntegration.getLinks("Add a step");
        Integer count = links.size();
        List<Integer> randomIndexes = new ArrayList<>();
        for (int i = 0; i < numberOfSteps; i++) {
            randomIndexes.add((int) Math.floor((Math.random() * count)));
        }
        for (int randomIndex : randomIndexes) {
            links.get(randomIndex).click();
            String stepType = "Basic Filter";
            String stepParameter = "ANY of the following, pathx " + randomIndex + ", Contains, valuex " + randomIndex;
            chooseStep.chooseStep(stepType);
            AbstractStep stepComponent = stepFactory.getStep(stepType, stepParameter);
            stepComponent.fillConfiguration();
            editor.getButton("Next").shouldBe(visible).click();
            addToIntegration.getButton("Add a Step").shouldBe(visible).click();
            list.add(randomIndex, stepParameter);
        }
        List<String> list2 = integrationFlowView.getStepsArray();
        for (int i = 0; i < list2.size(); i++) {
            log.info("assserting {} and {}", list.get(i), list2.get(i));
            assertThat(list.get(i), is(list2.get(i)));
        }
    }

    //what rest??
    @Then("^she deletes \"(\\d+)\" random integration steps and checks the rest$")
    public void deleteRandomStepsAndCheckRest(Integer numberOfSteps) {
        log.info("Deleting random phases");
        List<String> list = integrationFlowView.getStepsArray();
        ElementsCollection deletes = integrationFlowView.getAllTrashes().shouldBe(sizeGreaterThanOrEqual(1));
        int count = deletes.size();
        List<Integer> randomIndexes = new ArrayList<>();
        for (int i = 0; i < numberOfSteps; i++) {
            randomIndexes.add((int) Math.floor(Math.random() * (count - 2 - i)));
        }
        for (Integer randomIndex : randomIndexes) {
            deletes.get(randomIndex + 1).click();
            integrationFlowView.getFirstVisibleButton("OK").shouldBe(visible).click();
            list.remove(randomIndex);
        }
        List<String> list2 = integrationFlowView.getStepsArray();
        for (int i = 0; i < list.size(); i++) {
            log.info("assserting {} and {", list.get(i), list2.get(i));
            assertThat(list.get(i), is(list2.get(i)));
        }
    }

    //what rest???
    @Then("^she deletes step on position \"(\\d+)\" and checks the rest$")
    public void deleteStepOnPositionAndCheckRest(Integer positionOfStep) {
        log.info("Deleting step on position {}", positionOfStep);
        List<String> list = integrationFlowView.getStepsArray();
        ElementsCollection deletes = integrationFlowView.getAllTrashes().shouldBe(sizeGreaterThanOrEqual(1));
        Integer indexOfStep = positionOfStep + 1;
        deletes.get(indexOfStep).click();
        editor.getFirstVisibleButton("OK");
        list.remove(positionOfStep);
        //NOW CHECK:
        List<String> list2 = integrationFlowView.getStepsArray();
        for (int i = 0; i < list.size(); i++) {
            log.info("assserting {} and {}", list.get(i), list2.get(i));
            assertThat(list.get(i), is(list2.get(i)));
        }
    }

    /**
     * whether it's start or finish connection
     *
     * @param position
     */
    @Then("^she is prompted to select a \"([^\"]*)\" connection from a list of available connections$")
    public void verifyTypeOfConnection(String position) {
        log.info("{} connection must be active", position);
        assertTrue("There was no active icon found for position " + position, integrationFlowView.verifyActivePosition(position));
    }

    @When("^.*selects? the \"([^\"]*)\" connection$")
    public void selectConnection(String connectionName) {
        connectionsList.invokeActionOnItem(connectionName, ListAction.CLICK);
    }

}
