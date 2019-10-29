package io.syndesis.qe.pages.integrations.editor.add.steps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionalFlows {

    private static final class Elements {
        public static final By ADD_ANOTHER_CONDITION = ByUtils.dataTestId("button", "form-array-control-add-another-item-button");
        public static final By EVALUATED_PROPERTY_DROPDOWN_0 = ByUtils.dataTestId("input", "flowconditions-0-path");
    }

    public ConditionalFlows() {
    }

    public SelenideElement getAddAnotherConditionButton() {
        return $(Elements.ADD_ANOTHER_CONDITION).shouldBe(visible);
    }

    public SelenideElement getEvaluatedPropertyDropdown() {
        return $(Elements.EVALUATED_PROPERTY_DROPDOWN_0).shouldBe(visible);
    }
}
