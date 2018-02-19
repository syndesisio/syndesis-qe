package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import cucumber.api.java.en.Given;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.integration.Step;
import io.syndesis.model.integration.StepKind;
import io.syndesis.qe.utils.FilterRulesBuilder;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * This class contains intermediate step definition methods. It can for instance create a mapping or filter steps.
 *
 * Jan 15, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class IntermediateSteps {

    @Autowired
    private StepsStorage steps;

    public IntermediateSteps() {
    }

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
        steps.getSteps().add(mapperStep);
    }

    @Given("^create basic TW to SF filter step")
    public void createBasicFilterStep() {
        final Step basicFilter = new Step.Builder()
                .stepKind(StepKind.ruleFilter)
                .configuredProperties(TestUtils.map(
                        "type", "rule",
                        "predicate", FilterPredicate.AND.toString(),
                        "rules", new FilterRulesBuilder().addPath("text").addValue("#backendTest").addOps("contains").build()
                ))
                .build();
        steps.getSteps().add(basicFilter);
    }
}
