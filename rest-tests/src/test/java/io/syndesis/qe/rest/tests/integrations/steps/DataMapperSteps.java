package io.syndesis.qe.rest.tests.integrations.steps;

import java.util.Arrays;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.atlasmap.v2.MappingType;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.DataMapperStepDefinition;
import io.syndesis.qe.bdd.entities.SeparatorType;
import lombok.extern.slf4j.Slf4j;

/**
 * There are two ways how to specify data mapping. 1 is using preconfigured atlas mapping json file, with placeholders
 * instead of the connection ID's. This is easier to create, but more difficult to maintain. The second option is using
 * steps defined in gherkin scenario file.
 *
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DataMapperSteps extends AbstractStep {
    /**
     * Just creates mapper step definition, the mapper will be generated on the integration creation.
     *
     * @param mapperName
     */
    @When("start mapper definition with name: \"([^\"]*)\"")
    public void startMapperDefinition(String mapperName) {
        super.addProperty(StepProperty.STEP_NAME, mapperName);
        super.addProperty(StepProperty.KIND, StepKind.mapper);
        super.createStep();
    }

    // todo rework?
    @Then("^MAP using Step (\\d+) and field \"([^\"]*)\" to \"([^\"]*)\"$")
    public void mapDataMapperStep(int fromStep, String fromField, String toField) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(fromField));
        newDmStep.setOutputFields(Arrays.asList(toField));
        newDmStep.setMappingType(MappingType.MAP);
        newDmStep.setStrategy(null);
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @Then("^COMBINE using Step (\\d+) and strategy \"([^\"]*)\" into \"([^\"]*)\" and sources$")
    public void combineDataMapperStep(int fromStep, String strategy, String targetField, DataTable sourceMappingData) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(sourceMappingData.asList(String.class));
        newDmStep.setOutputFields(Arrays.asList(targetField));
        newDmStep.setMappingType(MappingType.COMBINE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @Then("^SEPARATE using Step (\\d+) and strategy \"([^\"]*)\" and source \"([^\"]*)\" into targets$")
    public void separateDataMapperStep(int fromStep, String strategy, String sourceField, DataTable targetMappingData) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(sourceField));
        newDmStep.setOutputFields(targetMappingData.asList(String.class));
        newDmStep.setMappingType(MappingType.SEPARATE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }
}
