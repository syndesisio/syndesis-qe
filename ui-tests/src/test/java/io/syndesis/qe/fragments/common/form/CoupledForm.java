package io.syndesis.qe.fragments.common.form;

import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoupledForm extends Form{

    private List<String> selectValues = new ArrayList<> (Arrays.asList("yes","checked","check","select","selected"));
    private List<String> unselectValues = new ArrayList<> (Arrays.asList("no","unchecked","uncheck","unselect","unselected"));

    public CoupledForm(SelenideElement rootElement) {
        super(rootElement);
    }

    @Override
    public void fill(Map<String, String> data) {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("There are no data to be filled into a form.");
        }

        for(String label: data.keySet()) {
            log.info("Filling form: " + label);

            //selecting element by a visible label (case insensitive)
            //can't use lower-case(...) method - not supported in Chrome and Firefox. Using translate(...) instead.
            SelenideElement element = $(By.xpath("//label[translate(normalize-space(text()),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='"
                    + label.toLowerCase() + "']/following-sibling::*[position()=1]/descendant-or-self::*[self::input or self::textarea or self::select]"));

            //fill input, textarea or select element
            switch (element.getTagName().toLowerCase()) {

                case "input":
                    String inputType = element.getAttribute("type");
                    //if the input is checkbox
                    if(inputType != null) {

                        switch(inputType.toLowerCase()) {

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
                                if(selectValues.contains(data.get(label).toLowerCase())) {
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
                                if(selectValues.contains(data.get(label).toLowerCase())) {
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
                            default: throw new UnsupportedOperationException(
                                    "Input element type " + "\"" + inputType.toLowerCase() + "\" can't be filled with the data.");
                        }
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
}
