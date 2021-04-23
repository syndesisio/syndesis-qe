package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.utils.ApicurioUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

public class ApicurioSteps {

    private static class PathElements {
        private static final By PARAMETERS_SECTION = By.cssSelector("path-params-section");
        private static final By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
    }

    /**
     * Second column: if true then create with link else create with plus sign.
     */
    @When("^create a new path with link$")
    public void createNewPathWithLink(DataTable table) {
        for (List<String> dataRow : table.cells()) {
            if (Boolean.parseBoolean(dataRow.get(1))) {
                ApicurioUtils
                    .getClickableLink("Add a path", ApicurioUtils.getAppRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                    .click();
            } else {
                ApicurioUtils.getNewPlusSignButton("section path-section panel-group",
                    ApicurioUtils.getAppRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                    .click();
            }

            ApicurioUtils.getLabelWithName("path", ApicurioUtils.getAppRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                .setValue("/" + dataRow.get(0));
            ApicurioUtils.getButtonWithText("Add", ApicurioUtils.getAppRoot()).shouldBe(visible, enabled).shouldNotHave(attribute("disabled"))
                .click();
        }
    }

    @When("^select path \"([^\"]*)\"$")
    public void selectPath(String path) {
        ApicurioUtils.getPathWithName(path).click();
    }

    /**
     * @param page can only have values "path" and "operations"
     */
    @When("^create path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void createPathParameter(String parameter, String page) {
        ApicurioUtils.createPathParameter(parameter, ApicurioUtils.getPageElement(page));
    }

    /**
     * @param page can only have values "path" and "operations"
     */
    @When("^set description \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setDescriptionPathParameter(String description, String parameter, String page) {
        SelenideElement root = ApicurioUtils.getPageElement(page);
        ApicurioUtils.openPathDescription(parameter, root);
        ApicurioUtils.setValueInTextArea(description, root.$(PathElements.PARAMETERS_SECTION));
    }

    /**
     * @param page can only have values "path" and "operations"
     */
    @When("^set path parameter type \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setPathParameterTypeForPathParameter(String type, String parameter, String page) {
        SelenideElement root = ApicurioUtils.getPageElement(page);
        ApicurioUtils.openPathTypes(parameter, root);
        SelenideElement parameterElement = root.$(PathElements.PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first();

        ApicurioUtils.setDropDownValue(ApicurioUtils.Buttons.PROPERTY_TYPE.getButtonId(), type, parameterElement);
    }

    /**
     * @param page can only have values "path" and "operations"
     */
    @When("^set path parameter type as \"([^\"]*)\" for path parameter \"([^\"]*)\" on \"([^\"]*)\" page")
    public void setPathParameterTypeAsForPathParameter(String as, String parameter, String page) {
        SelenideElement root = ApicurioUtils.getPageElement(page);
        ApicurioUtils.openPathTypes(parameter, root);
        SelenideElement parameterElement = root.$(PathElements.PARAMETERS_SECTION).$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first();

        ApicurioUtils.setDropDownValue(ApicurioUtils.Buttons.PROPERTY_TYPE_AS.getButtonId(), as, parameterElement);
    }

    /**
     * @param operation must be convertable to operation enum. See Operations enum
     */
    @When("^create new \"([^\"]*)\" operation$")
    public void createNewOperation(String operation) {
        ApicurioUtils.getCreateOperationButton(Operations.valueOf(operation))
            .click();
    }

    @When("^select operation \"([^\"]*)\"$")
    public void selectOperation(String operation) {
        ApicurioUtils
            .getOperationButton(Operations.valueOf(operation),
                ApicurioUtils.getAppRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
            .click();
    }

    @When("^set operation summary \"([^\"]*)\"$")
    public void setSummary(String summary) {
        ApicurioUtils.setValueInLabel(summary, ApicurioUtils.getOperationRoot().$(By.className("summary")), false);
    }

    @When("^set operation description \"([^\"]*)\"$")
    public void setDescription(String description) {
        ApicurioUtils.setValueInTextArea(description, ApicurioUtils.getOperationRoot().$(By.className("description")));
    }

    @When("^delete \"([^\"]*)\" operation$")
    public void deleteOperation(String operation) {
        ApicurioUtils.getOperationButton(ApicurioSteps.Operations.valueOf(operation),
            ApicurioUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
            .click();
        ApicurioUtils.deleteOperation();
    }

    /**
     * Data table params: response code --> 200, 404, ...
     * response definition
     * boolean true  --> create with plus sign
     * false --> create with link
     */
    @When("set response with plus sign")
    public void setResponseWithPlusSign(DataTable table) {
        for (List<String> dataRow : table.cells()) {
            if (Boolean.parseBoolean(dataRow.get(2))) {
                ApicurioUtils.getNewPlusSignButton("section responses-section panel-group",
                    ApicurioUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                    .click();
            } else {
                ApicurioUtils
                    .getClickableLink("Add a response",
                        ApicurioUtils.getOperationRoot().shouldBe(visible, enabled).shouldNotHave(attribute("disabled")))
                    .click();
            }
            ApicurioUtils.setResponseDetails(dataRow.get(0), dataRow.get(1));
        }
    }

    /**
     * Table parameters:
     * Type of dropdown                            --> type | of | as | required
     * Value of dropdown                           --> Array | String | Integer | ...
     * Page where dropdown is located              --> path | operations | datatypes
     * Section where dropdown is located           --> query | header | response | request body
     * Boolean if dropdown is in Responses section --> true it is , false otherwise
     * Number of response                          --> 100 | 200 | 404 | ...
     */
    @When("set parameters types")
    public void setParametersTypes(DataTable table) {       //TODO  add specific parameter when it will be needed

        for (List<String> dataRow : table.cells()) {
            String buttonId = ApicurioUtils.getButtonId(dataRow.get(0));
            SelenideElement page = ApicurioUtils.getPageElement(dataRow.get(2));
            By section = By.cssSelector("responses-section");

            if (Boolean.parseBoolean(dataRow.get(4))) {
                ApicurioUtils.selectResponse(dataRow.get(5));
            } else {
                ApicurioUtils.openCollapsedSection(page, section);
            }
            ApicurioUtils.setDropDownValue(buttonId, dataRow.get(1), page.$(section));
        }
    }

    @When("^set response description \"([^\"]*)\" for response \"([^\"]*)\"$")
    public void setDescriptionForResponse(String description, String response) {
        ApicurioUtils.selectResponse(response);
        ApicurioUtils.setValueInTextArea(description, ApicurioUtils.getOperationRoot().$(By.cssSelector("responses-section")));
    }

    @When("^(set|check) type of \"([^\"]*)\" media type (?:to|is) \"([^\"]*)\" on property \"([^\"]*)\" for response \"([^\"]*)\"$")
    public void setTypeOfMediaType(String check, String mediaType, String type, String property, String response) {
        ApicurioUtils.ensureMediaTypeExistsForResponse(mediaType, response);
        SelenideElement mediaRow = $$("media-type-row").find(text(mediaType));
        SelenideElement typeElement = mediaRow.$(".type");
        if (!typeElement.has(cssClass("selected"))) {
            typeElement.click();
        }
        String buttonId = ApicurioUtils.getButtonId(property);
        if ("set".equalsIgnoreCase(check)) {
            ApicurioUtils.setDropDownValue(buttonId, type, $("schema-type-editor"));
        } else {
            String value = mediaRow.$(buttonId).getText();
            assertThat(value).as("%s is %s but should be %s", property, value, type).isEqualTo(type);
        }
    }

    public enum Operations {
        GET,
        PUT,
        POST,
        DELETE,
        OPTIONS,
        HEAD,
        PATCH,
        TRACE
    }
}
