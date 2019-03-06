package io.syndesis.qe.steps.integrations.datamapper;

import static org.junit.Assert.assertThat;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.greaterThan;

import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/15/17.
 */
@Slf4j
public class DataMapperSteps {

    private DataMapper mapper = new DataMapper();

    @Deprecated
    @When("^create mapping from \"([^\"]*)\" to \"([^\"]*)\"$")
    public void createMapping(String source, String target) {
        mapper.createMapping(source, target);
    }

    /**
     * This step can create all types of data mapper mappings.
     * <p>
     * If you want to combine or separate functions, just use it as in this example:
     * <p>
     * Basic:           | user          | firstName             |
     * Combine:         | user; address | description           |
     * Separate:        | name          | firstName; lastName   |
     * <p>
     * For combine and separate, data mapper will automatically use default separator - space. Separator setting is not
     * implemented yet because it was not needed.
     *
     * @param table
     */
    @When("^create data mapper mappings$")
    public void createMapping(DataTable table) {
        mapper.openDataMapperCollectionElement();

        for (List<String> row : table.cells()) {
            mapper.doCreateMapping(row.get(0), row.get(1));
        }
    }

    @Then("^check visibility of data mapper ui$")
    public void dataMapperUIpresent() {
        log.info("data mapper ui must load and show fields count");
        try {
            OpenShiftWaitUtils.waitFor(() -> mapper.validate(), 1000 * 30);
        } catch (TimeoutException | InterruptedException e) {
            fail("Data mapper was not loaded in 30s!", e);
        }
        assertThat(mapper.fieldsCount(), greaterThan(0));
    }

    @When("^select \"([^\"]*)\" from \"([^\"]*)\" selector-dropdown$")
    public void selectFromDropDownByElement(String option, String selectAlias) {
        log.info(option);
        SelenideElement selectElement = mapper.getElementByAlias(selectAlias).shouldBe(visible);
        mapper.selectOption(selectElement, option);
    }

    @Then("^fill in \"([^\"]*)\" selector-input with \"([^\"]*)\" value$")
    public void fillActionConfigureField(String selectorAlias, String value) {
        SelenideElement inputElement = mapper.getElementByAlias(selectorAlias).shouldBe(visible);
        mapper.fillInput(inputElement, value);
    }

    /**
     * @param first     parameter to be combined.
     * @param first_pos position of the first parameter in the final string
     * @param second    parameter to be combined.
     * @param sec_pos   position of the second parameter in the final string.
     * @param combined  above two into this parameter.
     * @param separator used to estethically join first and second parameter.
     */
    @Deprecated
    // And she combines "FirstName" as "2" with "LastName" as "1" to "first_and_last_name" using "Space" separator
    @Then("^she combines \"([^\"]*)\" as \"([^\"]*)\" with \"([^\"]*)\" as \"([^\"]*)\" to \"([^\"]*)\" using \"([^\"]*)\" separator$")
    public void combinePresentFielsWithAnother(String first, String first_pos,
                                               String second, String sec_pos, String combined, String separator) {
        SelenideElement inputElement;
        SelenideElement selectElement;

        // Then fill in "FirstCombine" selector-input with "FirstName" value
        inputElement = mapper.getElementByAlias("FirstSource").shouldBe(visible);
        mapper.fillInput(inputElement, first);

        // And select "Combine" from "ActionSelect" selector-dropdown
        selectElement = mapper.getElementByAlias("ActionSelect").shouldBe(visible);
        mapper.selectOption(selectElement, "Combine");

        // And select "Space" from "SeparatorSelect" selector-dropdown
        selectElement = mapper.getElementByAlias("SeparatorSelect").shouldBe(visible);
        mapper.selectOption(selectElement, separator);

        // And click on the "Add Source" link
        mapper.getButton("Add Source").shouldBe(visible).click();

        // Then fill in "SecondCombine" selector-input with "LastName" value
        inputElement = mapper.getElementByAlias("SecondSource").shouldBe(visible);
        mapper.fillInputAndConfirm(inputElement, second);

        // And fill in "FirstCombinePosition" selector-input with "2" value
        inputElement = mapper.getElementByAlias("FirstSourcePosition").shouldBe(visible);
        mapper.fillInput(inputElement, first_pos);

        // And fill in "SecondCombinePosition" selector-input with "1" value
        inputElement = mapper.getElementByAlias("SecondSourcePosition").shouldBe(visible);
        mapper.fillInput(inputElement, sec_pos);

        // Then fill in "TargetCombine" selector-input with "first_and_last_name" value
//        inputElement = mapper.getElementByAlias("FirstTarget").shouldBe(visible);
//        mapper.fillInputAndConfirm(inputElement, combined);
    }

    //    And separate "FirstName" into "company" as "2" and "email" as "1" using "Comma" separator
    @Deprecated
    @Then("^separate \"([^\"]*)\" into \"([^\"]*)\" as \"([^\"]*)\" and \"([^\"]*)\" as \"([^\"]*)\" using \"([^\"]*)\" separator$")
    public void separatePresentFielsIntoTwo(String input, String output1, String first_pos, String output2, String second_pos, String separator) {
        SelenideElement inputElement;
        SelenideElement selectElement;

        //inputElement = mapper.getElementByAlias("FirstSource").shouldBe(visible);
        //mapper.fillInput(inputElement, input);

        selectElement = mapper.getElementByAlias("ActionSelect").shouldBe(visible);
        mapper.selectOption(selectElement, "Separate");

        selectElement = mapper.getElementByAlias("SeparatorSelect").shouldBe(visible);
        mapper.selectOption(selectElement, separator);

        // NOTE: THIS STEP SHOULD HAVE BEEN DONE AUTOMATICALLY BY SELECTING "Separate" action
        mapper.getButton("Add Target").shouldBe(visible).click();

        inputElement = mapper.getElementByAlias("FirstTarget").shouldBe(visible);
        mapper.fillInputAndConfirm(inputElement, output1);

        inputElement = mapper.getElementByAlias("FirstTargetPosition").shouldBe(visible);
        mapper.fillInput(inputElement, first_pos);

        inputElement = mapper.getElementByAlias("SecondTarget").shouldBe(visible);
        mapper.fillInputAndConfirm(inputElement, output2);

        inputElement = mapper.getElementByAlias("SecondTargetPosition").shouldBe(visible);
        mapper.fillInput(inputElement, second_pos);

    }

    @When("^define constant \"([^\"]*)\" of type \"([^\"]*)\" in data mapper$")
    public void defineConstantOfTypeInDataMapper(String value, String type) {
        mapper.addConstant(value, type);
    }
}
