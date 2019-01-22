package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.atlasmap.v2.MappingType;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.DataMapperDefinition;
import io.syndesis.qe.bdd.entities.DataMapperStepDefinition;
import io.syndesis.qe.bdd.entities.SeparatorType;
import io.syndesis.qe.bdd.entities.StepDefinition;

import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.utils.TestUtils;
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
public class DataMapperSteps {

    @Autowired
    private StepsStorage steps;

    /**
     * Step used for import of mapping json files. The step definition must contain a json file name, located in folder:
     * "resources/mappings"
     *
     * @param templateName
     * @throws IOException
     */
    @Given("^create mapper step using template: \"([^\"]*)\"")
    public void createMapperStep(String templateName) throws IOException {
        final String mapping = new String(Files.readAllBytes(Paths.get("./target/test-classes/mappings/" + templateName + ".json")));
        final Step mapperStep = new Step.Builder()
                .stepKind(StepKind.mapper)
                .configuredProperties(TestUtils.map("atlasmapping", mapping))
                .build();
        steps.getStepDefinitions().add(new StepDefinition(mapperStep));
    }

    /**
     * Just creates mapper step definition, the mapper will be generated on the integration creation.
     *
     * @param mapperName
     */
    @And("start mapper definition with name: \"([^\"]*)\"")
    public void startMapperDefinition(String mapperName) {
        final Step mapperStep = new Step.Builder()
                .stepKind(StepKind.mapper)
                .name(mapperName)
                .build();
        steps.getStepDefinitions().add(new StepDefinition(mapperStep, new DataMapperDefinition()));
    }

    @Then("MAP using Step (\\d+) and field \"([^\"]*)\" to \"([^\"]*)\"")
    public void mapDataMapperStep(int fromStep, String fromField, String toField) {

        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(fromField));
        newDmStep.setOutputFields(Arrays.asList(toField));
        newDmStep.setMappingType(MappingType.MAP);
        newDmStep.setStrategy(null);
        steps.getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @Then("COMBINE using Step (\\d+) and strategy \"([^\"]*)\" into \"([^\"]*)\" and sources$")
    public void combineDataMapperStep(int fromStep, String strategy, String targetField, DataTable sourceMappingData) {

        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(sourceMappingData.asList(String.class));
        newDmStep.setOutputFields(Arrays.asList(targetField));
        newDmStep.setMappingType(MappingType.COMBINE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        steps.getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @Then("SEPARATE using Step (\\d+) and strategy \"([^\"]*)\" and source \"([^\"]*)\" into targets$")
    public void separateDataMapperStep(int fromStep, String strategy, String sourceField, DataTable targetMappingData) {

        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(sourceField));
        newDmStep.setOutputFields(targetMappingData.asList(String.class));
        newDmStep.setMappingType(MappingType.SEPARATE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        steps.getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }
}
