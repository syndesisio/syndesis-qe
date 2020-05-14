package io.syndesis.qe.steps.integrations.datamapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.utils.ExcludeFromSelectorReports;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.List;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class DataMapperSteps {

    private DataMapper mapper = new DataMapper();

    /**
     * This step can create all types of data mapper mappings.
     * <p>
     * If you want to combine or separate functions, just use it as in this example:
     * <p>
     * Basic:                 | user          | firstName               |
     * Basic for collection:  | body.user     | response.user.firstName |
     * Combine:               | user; address | description             |
     * Separate:              | name          | firstName; lastName     |
     * <p>
     * For combine and separate, data mapper will automatically use default separator - space. Separator setting is not
     * implemented yet because it was not needed.
     *
     * @param table
     */
    @ExcludeFromSelectorReports
    @When("^create data mapper mappings$")
    public void createMapping(DataTable table) {

        // automatically open all collections for data mapping
        mapper.openDataMapperCollectionElement();

        for (List<String> row : table.cells()) {
            mapper.doCreateMapping(row.get(0), row.get(1));
        }
    }

    @ExcludeFromSelectorReports
    @Then("^check element with id \"([^\"]*)\" is present (\\d+) times$")
    public void sourceContainsElementsWithId(String id, int numberOfElements) {
        assertThat(mapper.getAllItemsWithName(id)).hasSize(numberOfElements);
    }

    @ExcludeFromSelectorReports
    @Then("^check visibility of data mapper ui$")
    public void dataMapperUIpresent() {
        log.info("data mapper ui must load and show fields count");
        try {
            OpenShiftWaitUtils.waitFor(() -> mapper.validate(), 1000 * 30);
        } catch (TimeoutException | InterruptedException e) {
            fail("Data mapper was not loaded in 30s!", e);
        }
    }

    @ExcludeFromSelectorReports
    @When("^define constant \"([^\"]*)\" of type \"([^\"]*)\" in data mapper$")
    public void defineConstantOfTypeInDataMapper(String value, String type) {
        mapper.addConstant(value, type);
    }

    @ExcludeFromSelectorReports
    @When("^define property \"([^\"]*)\" with value \"([^\"]*)\" of type \"([^\"]*)\" in data mapper$")
    public void definePropertyWithValueOfTypeInDataMapper(String name, String value, String type) {
        mapper.addProperty(name, value, type);
    }
}
