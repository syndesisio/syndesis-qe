package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalFlowSteps {
    private static class EditFlowStepElements {
        private static By ICON_DOWN = By.className("fa-arrow-circle-o-down");
        private static By ICON_UP = By.className("fa-arrow-circle-o-up");
        private static By ICON_DELETE = By.className("fa-trash-o");
        private static By CONDITION_LIST_ITEM = By.className("form-control");
        private static By OPEN_FLOW_CONDITION_BUTTON_PARENT = By.className("list-view-pf-actions");
        private static By FLOW_DROPDOWN_CONTAINER = By.className("pf-c-dropdown");
        private static By FLOW_DROPDOWN = By.className("pf-c-dropdown__toggle-icon");
        private static By FLOW_DROPDOWN_ITEM = By.className("pf-c-dropdown__menu-item");
        private static By FLOW_BACK_BUTTON = By.cssSelector("a[data-testid=\"conditions-back-button-item-back-button\"]");

        private static By getConditionOnPosition(String position) {
            return By.cssSelector("[data-testid=\"flowconditions-X-condition\"]".replace("X", position));
        }

        private static By getConditionIconsOnPosition(String position) {
            return By.id("flowconditions-X-array-controls-control".replace("X", position));
        }
    }

    private static class EditIntegrationElements {
        private static By CONDITIONAL_FLOW_STEP = By.cssSelector("[data-testid=\"integration-editor-steps-list-item-conditional-flows-list-item\"]");
        private static By CONDITIONAL_FLOW_STEP_INNER_FLOW_ITEM = By.className("list-group-item");
        private static By DEFAULT_DISABLED = By.className("fa-ban");
    }

    //data:     |position   |value  |
    //example:  |0          |asdf   |
    @Then("^validate condition content in condition flow step$")
    public void validateConditionContent(DataTable data) {

        for (List<String> row : data.cells()) {
            assertThat($(EditFlowStepElements.getConditionOnPosition(row.get(0))).getValue())
                .isEqualToIgnoringCase(row.get(1));

            //selenide returns empty value in some cases even if the value is present, probably a driver bug
            // so second assert with usage of javascript is necessary
            assertThat((String) executeJavaScript("return document.getElementById('flowconditions-" + row.get(0) + "-condition').value"))
                .isEqualToIgnoringCase(row.get(1));
        }
    }

    //data:
    //  |position   |ACTION   |
    //  |1          |UP       |
    //  |0          |DOWN     |
    //  |1          |DELETE   |
    @When("^click on the condition icon$")
    public void clickOnConditionIcon(DataTable data) {
        for (List<String> row : data.cells()) {
            SelenideElement selectedCondition = $(EditFlowStepElements.getConditionIconsOnPosition(row.get(0))).shouldBe(visible);
            By iconSelector;
            switch (row.get(1)) {
                case "UP":
                    iconSelector = EditFlowStepElements.ICON_UP;
                    break;
                case "DOWN":
                    iconSelector = EditFlowStepElements.ICON_DOWN;
                    break;
                case "DELETE":
                    iconSelector = EditFlowStepElements.ICON_DELETE;
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect data table value on position 1");
            }
            selectedCondition.$(iconSelector).shouldBe(visible).click();
        }
    }

    @Then("^validate that condition count is equal to (.)$")
    public void validateConditionCount(int count) {
        TestUtils.waitFor(() -> $$(EditFlowStepElements.CONDITION_LIST_ITEM).size() > 0,
            1, 20, "No condition found");

        assertThat($$(EditFlowStepElements.CONDITION_LIST_ITEM)).hasSize(count);
    }

    @Then("^configure condition on position (.)$")
    public void configureConditionOnPosition(int index) {
        TestUtils.waitFor(() -> $$(EditFlowStepElements.OPEN_FLOW_CONDITION_BUTTON_PARENT).size() > index,
            3, 30, "Condition on position " + index + " was not found");

        $$(EditFlowStepElements.OPEN_FLOW_CONDITION_BUTTON_PARENT).get(index).shouldBe(visible)
            .$(By.tagName("a")).shouldBe(visible).click();
    }

    @When("^return to primary flow from integration flow$")
    public void goBackToPrimaryFlow() {
        openDropdownWithConditions();
        $(EditFlowStepElements.FLOW_BACK_BUTTON).shouldBe(visible).click();
    }

    @Then("^check that conditional flow step contains (\\d+) flows$")
    public void validateNumberOfFlowsInIntegrationEditView(int expectedNumberOfFlows) {
        int size = $(EditIntegrationElements.CONDITIONAL_FLOW_STEP).shouldBe(visible)
            .$$(EditIntegrationElements.CONDITIONAL_FLOW_STEP_INNER_FLOW_ITEM).size();
        assertThat(size).isEqualTo(expectedNumberOfFlows);
    }

    @Then("^check that conditional flow default step is (disabled|enabled)$")
    public void validateStateOfDefaultFlow(String state) {
        assertThat(!$(EditIntegrationElements.DEFAULT_DISABLED).exists())
            .isEqualTo("enabled".equalsIgnoreCase(state));
    }

    @Then("^validate conditional flow dropdown content$")
    public void validateConditionalFlowDropdown(DataTable data) {
        openDropdownWithConditions();

        ElementsCollection dropdownElements = $$(EditFlowStepElements.FLOW_DROPDOWN_ITEM);
        int current = 0;
        for (List<String> row : data.cells()) {
            assertThat(dropdownElements.get(current).getText().replaceAll("\n", " ")).isEqualToIgnoringCase(row.get(0));
            current++;
        }
    }

    private void openDropdownWithConditions() {
        if ($(By.className("pf-c-dropdown__menu")).exists()) {
            return;
        }

        TestUtils.waitFor(() -> {
            List<SelenideElement> flowDropdown =
                $$(EditFlowStepElements.FLOW_DROPDOWN_CONTAINER)
                    .stream()
                    .filter(e -> e.$(EditFlowStepElements.FLOW_DROPDOWN).exists())
                    .collect(Collectors.toList());
            return flowDropdown.size() >= 1;
        }, 3, 30, "Back to Primary Flow button was not found");

        log.info("dropdown for back button was found!");

        List<SelenideElement> flowDropdown =
            $$(EditFlowStepElements.FLOW_DROPDOWN_CONTAINER)
                .stream()
                .filter(e -> e.$(EditFlowStepElements.FLOW_DROPDOWN).exists())
                .filter(e -> e.parent().text().contains("Flow"))
                .map(e -> e.$(By.tagName("button")))
                .collect(Collectors.toList());
        assertThat(flowDropdown).hasSize(1);
        flowDropdown.get(0).click();
    }
}
