package io.syndesis.qe.fragments.common.form;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.syndesis.qe.pages.integrations.editor.ApiProviderOperationEditorPage;
import io.syndesis.qe.utils.OpenShiftUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Getter
@Slf4j
public class Form {
    private SelenideElement rootElement;
    private final List<String> selectValues = new ArrayList<>(Arrays.asList("yes", "checked", "check", "select", "selected", "true"));
    private final List<String> unselectValues = new ArrayList<>(Arrays.asList("no", "unchecked", "uncheck", "unselect", "unselected", "false"));


    public Form(SelenideElement rootElement) {
        this.rootElement = rootElement;
    }

    public void fillByName(Map<String, String> data) {
        fillBy(FillBy.NAME, data);
    }

    public void fillById(Map<String, String> data) {
        fillBy(FillBy.ID, data);
    }

    public void fillByTestId(Map<String, String> data) {
        fillBy(FillBy.TEST_ID, data);
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

        // this is an inputs map, which contains inputs and their respective types (tag names) on the form
        // that we are trying to fill
        // here, key is attribute (name or id) value of the input
        // and value of the map element pair is element's tag for later use
        Map<String, String> inputsMap = new HashMap<>();
        for (String tagName : Arrays.asList("input", "select", "textarea")) {
            for (SelenideElement element : getRootElement().findAll(By.tagName(tagName))) {
                inputsMap.put(element.getAttribute(fillBy.attribute), tagName);
            }
        }

        log.info(inputsMap.toString());
        for (String key : data.keySet()) {
            // if the element we're looking for is on the form, then fill, otherwise error
            if (inputsMap.containsKey(key)) {
                log.info("fill value in {} ", key);
                SelenideElement input;

                if (fillBy.attribute.equalsIgnoreCase("id")) {
                    input = getRootElement().$(By.id(key)).shouldBe(visible);
                } else {
                    String cssSelector = String.format("%s[" + fillBy.attribute + "='%s']", inputsMap.get(key), key);
                    input = getRootElement().$(cssSelector).shouldBe(visible);
                }

                if (input.parent().$(By.tagName("select")).exists()) {
                    log.debug("trying to set " + data.get(key) + " into element" + input.toString());
                    input.selectOptionContainingText(data.get(key));
                } else if ("checkbox".equals(input.getAttribute("type"))) {
                    // we only want to click the checkbox if it's current state and desired state are different
                    boolean shouldClick = input.isSelected() != BooleanUtils.toBoolean(data.get(key));
                    if (shouldClick) {
                        input.click();
                    }
                } else {
                    input.clear();
                    input.sendKeys(data.get(key));

                }
            } else {
                log.warn("Input {} is not present on form!", key);
            }
        }
    }

    public void fillEditor(String data) {
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
                            + label.toLowerCase() + "']/following-sibling::*[position()=1]/descendant-or-self::*[self::input or self::textarea or self::select]"
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

    public static void waitForInpups(int timeInSeconds) {
        $(By.cssSelector(".form-control")).waitUntil(exist, timeInSeconds * 1000);
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
