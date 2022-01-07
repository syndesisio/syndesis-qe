package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.stream.Collectors;

public class DataTypeParametersDialog extends ModalDialogPage {

    private static final class Element {
        static final By PARAMETER_ROOT = By.className("pf-c-form__group");
        static String PARAMETER_UNIQUE_SELECTOR = "%d-parameter";
        static final By PARAMETER_SELECT = By.id("selected-paramater");

        static final By TEXT_AREA = ByUtils.customAttribute("type", "text");
    }

    private static final class Button {
        static final By ADD_PARAMETER = ByUtils.partialLinkText("Add parameter");
        static final By BOOLEAN_SWITCHER = By.className("pf-c-switch__toggle");
        static final By DELETE_BUTTON = By.className("pf-c-button");
    }

    public void closeDialog() {
        getRootElement().find(ByUtils.dataTestId("confirmation-dialog-confirm-button")).shouldBe(visible).click();
    }

    public void addParameter(String paramName, String value) {
        int numberOfParams = getNumberOfParameters();
        getRootElement().find(Button.ADD_PARAMETER).shouldBe(visible).click();
        SelenideElement newParameterRootElement = getParameterRootElement(numberOfParams + 1);
        newParameterRootElement.find(Element.PARAMETER_SELECT).selectOptionContainingText(paramName);
        changeParamValue(newParameterRootElement, value);
    }

    public void deleteParameter(String paramName) {
        getParameterRootElement(paramName).find(Button.DELETE_BUTTON).shouldBe(visible).click();
    }

    public List<String> getExistedParameters() {
        // get only param name which is saved in the `label` attribute
        return getRootElement().findAll(Element.PARAMETER_SELECT).stream().map(el -> el.getAttribute("label")).collect(Collectors.toList());
    }

    public void updateExistedParameter(String paramName, String value) {
        changeParamValue(getParameterRootElement(paramName), value);
    }

    public boolean getBooleanValueOfParameter(String paramName) {
        return getParameterBoolean(getParameterRootElement(paramName));
    }

    private boolean getParameterBoolean(SelenideElement parameterRoot) {
        return parameterRoot.find(By.tagName("input")).getAttribute("checked") != null;
    }

    /**
     * Get parameter root element by position
     */
    private SelenideElement getParameterRootElement(int i) {
        return getRootElement().findAll(Element.PARAMETER_ROOT).stream().filter(
            el -> el.find(ByUtils.customAttribute("for", String.format(Element.PARAMETER_UNIQUE_SELECTOR, i))).exists()
        ).findFirst().get();
    }

    /**
     * Get parameter root element by parameter name
     */
    private SelenideElement getParameterRootElement(String name) {
        return getRootElement().findAll(Element.PARAMETER_ROOT).stream().filter(
            el -> el.find(Element.PARAMETER_SELECT).exists() && el.find(Element.PARAMETER_SELECT).getAttribute("label").contains(name)
        ).findFirst().get();
    }

    private int getNumberOfParameters() {
        return getRootElement().findAll(Element.PARAMETER_SELECT).size();
    }

    private void changeParamValue(SelenideElement parameterRootElement, String value) {
        if ("true".equals(value) || "false".equals(value)) {
            //boolean input
            boolean desiredStatus = Boolean.parseBoolean(value);
            boolean actualStatus = getParameterBoolean(parameterRootElement);
            if (actualStatus != desiredStatus) {
                parameterRootElement.find(Button.BOOLEAN_SWITCHER).shouldBe(visible).click();
            }
        } else {
            //text input
            parameterRootElement.find(Element.TEXT_AREA).shouldBe(visible).sendKeys(value);
        }
    }
}
