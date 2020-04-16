package io.syndesis.qe.fragments.common.form;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Form {
    private SelenideElement rootElement;
    private final List<String> selectValues = new ArrayList<>(Arrays.asList("yes", "checked", "check", "select", "selected", "true"));
    private final List<String> unselectValues = new ArrayList<>(Arrays.asList("no", "unchecked", "uncheck", "unselect", "unselected", "false"));

    private static final class Elements {
        private static By DROPDOWN_TOGGLE = By.className("pf-c-dropdown__toggle");
        private static By DROPDOWN_MENU_ITEM = By.className("pf-c-dropdown__menu-item");
        private static By DROPDOWN_MENU = By.className("pf-c-dropdown__menu");
    }

    public Form(SelenideElement rootElement) {
        this.rootElement = rootElement;
    }

    public void fillByName(Map<String, String> data) {
        fillBy(FillBy.NAME, data);
    }

    public void fillById(Map<String, String> data) {
        fillBy(FillBy.ID, data);
    }

    /**
     * Fill elements by data testid but skip non-existent elements
     *
     * @param data
     */
    public void fillByTestId(Map<String, String> data) {
        fillBy(FillBy.TEST_ID, data);
    }

    /**
     * Test if all input elements exist then fill them by data testid
     *
     * @param data
     */
    public void forceFillByTestId(Map<String, String> data) {
        for (String testId : data.keySet()) {
            TestUtils.waitFor(() -> $(ByUtils.dataTestId(testId)).exists(), 1, 15, "Fill in element " + testId + " was not found");
        }

        fillBy(FillBy.TEST_ID, data);
    }

    /**
     * This is an inputs map, which contains inputs and their respective types (tag names) on the form
     * that we are trying to fill.
     * Here, key is attribute (name or id) value of the input
     * and value of the map element pair is element's tag for later use
     *
     * @param fillBy [attribute - value]
     */
    private Map<String, String> getInputMap(FillBy fillBy) {
        Map<String, String> inputsMap = new HashMap<>();
        TestUtils.waitFor(() -> getRootElement().shouldBe(visible).exists(),
            1, 20, "Form root element not found in 20s");
        for (String tagName : Arrays.asList("input", "select", "textarea", "button", "div", "checkbox")) {
            for (SelenideElement element : getRootElement().shouldBe(visible).findAll(By.tagName(tagName))) {
                inputsMap.put(element.getAttribute(fillBy.attribute), tagName);
            }
        }
        return inputsMap;
    }

    /**
     * Finds the input elements by element attribute and fills the data in
     *
     * @param data [attribute - value]
     */
    public void fillBy(FillBy fillBy, Map<String, String> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("account data is not set (empty)");
        }
        Map<String, String> inputsMap = getInputMap(fillBy);

        log.info(inputsMap.toString());
        for (String key : data.keySet()) {
            // if the element we're looking for is on the form, then fill, otherwise error
            if (inputsMap.containsKey(key)) {
                log.info("fill value in {} ", key);
                SelenideElement input;
                //1. finding input element:
                if (fillBy.attribute.equalsIgnoreCase("id")) {
                    input = getRootElement().$(By.id(key)).shouldBe(visible);
                } else {
                    String cssSelector = String.format("%s[" + fillBy.attribute + "='%s']", inputsMap.get(key), key);
                    input = getRootElement().$(cssSelector).shouldBe(visible);
                }

                //2.filling input element with value:
                if (input.is(Condition.type("button")) && input.parent().has(Condition.cssClass("dropdown"))) {
                    input.click();
                    input.parent().$$(By.tagName("a")).find(Condition.exactText(data.get(key))).click();
                } else if (input.parent().$(By.tagName("select")).exists()) {
                    log.debug("trying to set " + data.get(key) + " into element" + input.toString());
                    input.selectOptionContainingText(data.get(key));
                } else if ("checkbox".equals(input.getAttribute("type"))) {
                    // we only want to click the checkbox if it's current state and desired state are different
                    boolean shouldClick = input.isSelected() != BooleanUtils.toBoolean(data.get(key));
                    if (shouldClick) {
                        input.click();
                    }
                } else if ("div".equals(inputsMap.get(key))) {
                    //1. click on toggle-icon:
                    openDivDropdownWithConditions(input);
                    //2. check if value is present in dropdown menu and select value:
                    assertThat(selectDivDropdownValue(input, data.get(key))).isTrue();
                } else {
                    //it means, input is "input" or "textarea" type
                    input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
                    input.sendKeys(Keys.BACK_SPACE);
                    input.clear();
                    input.sendKeys(data.get(key));
                }
            } else {
                log.warn("Input {} is not present on form!", key);
            }
        }
    }

    public void fillEditor(String data) {
        TestUtils.waitFor(() -> $(By.className("CodeMirror")).exists(),
            1, 20, "Text editor was not loaded in 20s");

        SelenideElement editor = $(By.className("CodeMirror")).shouldBe(visible);
        WebDriver driver = WebDriverRunner.getWebDriver();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", editor, data);
        WebDriverWait wait = new WebDriverWait(driver, 20);
    }

    /**
     * Finds the input elements by label and fills the data in
     *
     * @param data [label - value]
     */
    public void fillByLabel(Map<String, String> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("There are no data to be filled into a form.");
        }

        for (String label : data.keySet()) {
            log.info("Filling form: " + label);

            //selecting element by a visible label (case insensitive)
            //can't use lower-case(...) method - not supported in Chrome and Firefox. Using translate(...) instead.

            String xpath =

                /**
                 * regular input fields:
                 * field label [              ]
                 *
                 * <label>label</label>
                 * <?>
                 *      <input, textarea, select> <.../>
                 * <?/>
                 */

                "//label[translate(normalize-space(text()),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
                    + label.toLowerCase() +
                    "']/following-sibling::*[position()=1]/descendant-or-self::*[self::input or self::textarea or self::select]"
                    + "|"

                    /**
                     * usually checkboxes:
                     * [] checkbox label
                     *
                     * <label>
                     *      <input, textarea, select></>
                     *      <span>label<span/>
                     * </label>
                     *
                     */
                    + "//label/span[translate(normalize-space(text()),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
                    + label.toLowerCase() + "']/preceding-sibling::*[position()=1]/self::*[self::input or self::textarea or self::select]";

            SelenideElement element = $(By.xpath(xpath)).shouldBe(visible);

            //fill input, textarea or select element
            switch (element.getTagName().toLowerCase()) {

                case "input":
                    String inputType = element.getAttribute("type");
                    if (inputType == null) {
                        //default value
                        inputType = "text";
                    }

                    switch (inputType.toLowerCase()) {

                        case "":
                        case "text":
                        case "password":
                        case "date":
                        case "datetime":
                        case "datetime-local":
                        case "email":
                        case "month":
                        case "number":
                        case "search":
                        case "tel":
                        case "time":
                        case "url":
                        case "week":
                            element.setValue(data.get(label));
                            break;

                        case "radio":
                            //can be only selected. Unselecting is done by selecting other radiobutton in a group
                            if (selectValues.contains(data.get(label).toLowerCase())) {
                                element.setSelected(true);
                            } else {
                                throw new UnsupportedOperationException(
                                    "Unknown value <" + data.get(label) + "> for radiobutton \""
                                        + label + "\": <" + "data.get(label)" + ">. " +
                                        "The following case insensitive values can be used: \n [" +
                                        "" + String.join(", ", selectValues) + "]");
                            }

                        case "checkbox":
                            //checkbox can be either checked or unchecked
                            if (selectValues.contains(data.get(label).toLowerCase())) {
                                element.setSelected(true);
                                //uncheck
                            } else if (unselectValues.contains(data.get(label).toLowerCase())) {
                                element.setSelected(false);
                                //unsupported value
                            } else {
                                throw new UnsupportedOperationException(
                                    "Unknown value <" + data.get(label) + "> for checkbox \""
                                        + label + "\": <" + data.get(label) + ">. " +
                                        "The following case insensitive values can be used: \n" +
                                        "checked checkbox: [" + String.join(", ", selectValues) + "]" + "\n" +
                                        "unchecked checkbox: [" + String.join(", ", unselectValues) + "]");
                            }
                            break;

                        case "color":
                        case "range":
                        default:
                            throw new UnsupportedOperationException(
                                "Input element type " + "\"" + inputType.toLowerCase() + "\" can't be filled with the data.");
                    }

                    break;

                case "textArea":
                    element.setValue(data.get(label));
                    break;

                case "select":
                    //selecting by value visible to a user
                    element.selectOptionContainingText(data.get(label));
                    break;
                default:
            }
        }
    }

    public static void waitForInputs(int timeInSeconds) {
        $(By.cssSelector("form input,select")).waitUntil(exist, timeInSeconds * 1000);
    }

    public boolean selectDivDropdownValue(SelenideElement parent, String itemValue) {

        ElementsCollection dropdownElements = parent.$$(Elements.DROPDOWN_MENU_ITEM);

        for (SelenideElement item : dropdownElements) {
            if (itemValue.equals(item.getText())) {
                item.click();
                return true;
            }
        }
        return false;
    }

    private void openDivDropdownWithConditions(SelenideElement parent) {
        if (parent.$(Elements.DROPDOWN_MENU).exists()) {
            return;
        }
        SelenideElement dropdownToggle = parent.$(Elements.DROPDOWN_TOGGLE).waitUntil(visible, 30000);
        dropdownToggle.click();
    }

    public void checkByTestId(List<String> values) {
        checkBy(FillBy.TEST_ID, values);
    }

    private void checkBy(FillBy fillBy, List<String> values) {
        Map<String, String> inputsMap = getInputMap(fillBy);
        for (String value : values) {
            // if the element we're looking for is on the form, then fill, otherwise error
            if (inputsMap.containsKey(value)) {
                String cssSelector = String.format("%s[" + fillBy.attribute + "='%s']", inputsMap.get(value), value);
                SelenideElement el = getRootElement().$(cssSelector).shouldBe(visible);
                log.info("ELEMENT: *{}*", el.toString());
            }
        }
        log.info("All required fields are visible!");
    }

    private enum FillBy {
        ID("id"),
        NAME("name"),
        TEST_ID("data-testid");

        public final String attribute;

        FillBy(String att) {
            attribute = att;
        }
    }
}
