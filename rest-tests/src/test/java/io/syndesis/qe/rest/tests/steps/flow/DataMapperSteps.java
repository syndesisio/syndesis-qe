package io.syndesis.qe.rest.tests.steps.flow;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.entities.DataMapperStepDefinition;
import io.syndesis.qe.bdd.entities.SeparatorType;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.v2.MappingType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

/**
 * There are two ways how to specify data mapping. 1 is using preconfigured atlas mapping json file, with placeholders
 * instead of the connection ID's. This is easier to create, but more difficult to maintain. The second option is using
 * steps defined in gherkin scenario file.
 *
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
public class DataMapperSteps extends AbstractStep {
    /**
     * Just creates mapper step definition, the mapper will be generated on the flow creation.
     *
     * @param mapperName
     */
    @When("^start mapper definition with name: \"([^\"]*)\"$")
    public void startMapperDefinition(String mapperName) {
        super.addProperty(StepProperty.STEP_NAME, mapperName);
        super.addProperty(StepProperty.KIND, StepKind.mapper);
        super.createStep();
    }

    @When("^MAP using Step (\\d+) and field \"([^\"]*)\" to \"([^\"]*)\"$")
    public void mapDataMapperStep(int fromStep, String fromField, String toField) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(fromField));
        newDmStep.setOutputFields(Arrays.asList(toField));
        newDmStep.setMappingType(MappingType.MAP);
        newDmStep.setStrategy(null);
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @When("^COMBINE using Step (\\d+) and strategy \"([^\"]*)\" into \"([^\"]*)\" and sources$")
    public void combineDataMapperStep(int fromStep, String strategy, String targetField, DataTable sourceMappingData) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(sourceMappingData.asList(String.class));
        newDmStep.setOutputFields(Arrays.asList(targetField));
        newDmStep.setMappingType(MappingType.COMBINE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @When("^SEPARATE using Step (\\d+) and strategy \"([^\"]*)\" and source \"([^\"]*)\" into targets$")
    public void separateDataMapperStep(int fromStep, String strategy, String sourceField, DataTable targetMappingData) {
        DataMapperStepDefinition newDmStep = new DataMapperStepDefinition();
        newDmStep.setFromStep(fromStep);
        newDmStep.setInputFields(Arrays.asList(sourceField));
        newDmStep.setOutputFields(targetMappingData.asList(String.class));
        newDmStep.setMappingType(MappingType.SEPARATE);
        newDmStep.setStrategy(SeparatorType.valueOf(strategy));
        super.getSteps().getLastStepDefinition().getDataMapperDefinition().get().getDataMapperStepDefinition().add(newDmStep);
    }

    @When("^add \"([^\"]*)\" transformation on \"([^\"]*)\" field with id \"([^\"]*)\" with properties")
    public void addTransformation(String transformation, String target, String id, DataTable properties) {
        try {
            Class<?> c = Class.forName("io.atlasmap.v2." + StringUtils.capitalize(transformation));
            Constructor<?> cons = c.getConstructor();
            Object t = cons.newInstance();
            Map<String, String> stringProperties = properties.asMap(String.class, String.class);
            for (Map.Entry<String, String> entry : stringProperties.entrySet()) {
                Field f = c.getDeclaredField(entry.getKey());
                f.setAccessible(true);
                f.set(t, entry.getValue());
            }

            Map<String, Map<String, List<Object>>> transformations = super.getSteps().getLastStepDefinition().getDataMapperDefinition()
                    .get().getLastDatamapperStepDefinition().getTransformations();
            transformations.computeIfAbsent(target, v -> new HashMap<>());
            transformations.get(target).computeIfAbsent(id, v -> new ArrayList<>());
            transformations.get(target).get(id).add(t);
        } catch (Exception e) {
            fail("Unable to create atlasmap class for " + transformation, e);
        }
    }
}
