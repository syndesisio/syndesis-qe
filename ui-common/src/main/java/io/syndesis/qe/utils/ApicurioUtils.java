package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.steps.other.ApicurioSteps;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicurioUtils {
    private static class PathElements {
        private static final By PATH_PARAMETERS_ROW = By.cssSelector("path-param-row");
        private static final By RESPONSES_SECTION = By.cssSelector("responses-section");
    }

    public enum Buttons {
        PROPERTY_TYPE("#api-property-type"),
        PROPERTY_TYPE_OF("#api-property-type-of"),
        PROPERTY_TYPE_AS("#api-property-type-as"),
        PROPERTY_REQUIRED("#api-property-required"),

        MEDIA_TYPE("#api-mediaType");

        private final String buttonId;

        Buttons(String buttonId) {
            this.buttonId = buttonId;
        }

        public String getButtonId() {
            return this.buttonId;
        }
    }

    public static SelenideElement getPathPageRoot() {
        return $("path-form").shouldBe(visible);
    }

    public static SelenideElement getPathWithName(String pathName) {
        log.info("Getting path with name {}", pathName);

        ElementsCollection allPaths = getAppRoot().$("section[type=\"path\"]").findAll(By.cssSelector("div[class=\"api-path-item\"]"));
        for (SelenideElement path : allPaths) {
            if (path.getText().equals(pathName)) {
                return path;
            }
        }
        //path is not found
        return null;
    }

    public static SelenideElement getCreateOperationButton(ApicurioSteps.Operations operation) {
        log.info("searching for add operation button for operation {}", operation);

        ElementsCollection
            operations = getPathPageRoot().$$("div").filter(attribute("class", "operation-tab " + operation.toString().toLowerCase() + "-tab"));
        if (operations.size() > 0) {     //if size is 0, operation tab is already selected
            operations.first().click();
        } else {
            //Check that selected operation is ours $operation
            assertThat(getPathPageRoot().$$("div").filter(attribute("class", "operation-tab " + operation.toString().toLowerCase() + "-tab selected"))
                .size()).isEqualTo(1);
        }
        return getButtonWithText("Add Operation", getPathPageRoot());
    }

    public static SelenideElement getOperationButton(ApicurioSteps.Operations operation, SelenideElement differentRoot) {
        log.info("searching for operation button for operation {}", operation);

        return differentRoot.$(By.className(operation.toString().toLowerCase() + "-tab")).shouldBe(visible, Duration.ofSeconds(5)).shouldBe(enabled);
    }

    public static void createPathParameter(String parameter, SelenideElement root) {
        getButtonWithText("Create", root.$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first()).click();
    }

    public static void openPathDescription(String parameter, SelenideElement root) {
        ElementsCollection elements = root.$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first()
            .$$("div").filter(attribute("class", "description"));
        if (elements.size() == 1) {
            elements.first().click();
        }
    }

    public static void openPathTypes(String parameter, SelenideElement root) {
        ElementsCollection elements = root.$$(PathElements.PATH_PARAMETERS_ROW)
            .filter(text(parameter)).first()
            .$$("div").filter(attribute("class", "summary"));
        if (elements.size() == 1) {
            elements.first().click();
        }
    }

    public static SelenideElement getButtonWithText(String buttonText, SelenideElement differentRoot) {
        log.info("searching for button {}", buttonText);

        return differentRoot.shouldBe(visible).$$("button").filter(text(buttonText))
            .shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    public static SelenideElement getNewPlusSignButton(String sectionName, SelenideElement differentRoot) {
        log.info("searching for a new plus sign button for section {}", sectionName);

        return differentRoot.$$("div").filter(attribute("class", sectionName)).first().$("button");
    }

    public static SelenideElement getButtonWithTitle(String buttonTitle, SelenideElement differentRoot) {
        log.info("searching for button with title {}", buttonTitle);

        return differentRoot.shouldBe(visible).$$("button").filter(attribute("title", buttonTitle))
            .shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    public static SelenideElement getLabelWithName(String labelName, SelenideElement differentRoot) {
        log.info("searching for label {}", labelName);
        return differentRoot.shouldBe(visible).find(By.cssSelector(String.format("input[name*=\"%s\"]", labelName)));
    }

    public static SelenideElement getLabelWithType(String labelType, SelenideElement differentRoot) {
        log.info("searching for label {}", labelType);
        return differentRoot.shouldBe(visible).$$("input").filter(attribute("type", labelType)).first();
    }

    public static SelenideElement getClickableLink(String sectionA, SelenideElement differentRoot) {
        log.info("searching for link in section {}", sectionA);
        return differentRoot.$$("a").find(text(sectionA));
    }

    public static SelenideElement getAppRoot() {
        return $(By.cssSelector("app-root")).shouldBe(visible);
    }

    public static SelenideElement getOperationRoot() {
        return $("operations-section").shouldBe(visible);
    }

    public static void deleteOperation() {
        getOperationRoot().$(By.className("actions")).shouldBe(visible).click();
        getDropdownMenuItem("Delete Operation").shouldBe(visible).click();
    }

    public static SelenideElement getPageElement(String pageName) {
        switch (pageName) {
            case "operations":
                return getOperationRoot();
            case "path":
                return ApicurioUtils.getPathPageRoot();
        }
        return null;
    }

    public static void setResponseDetails(String code, String responseDef) {
        log.info("Setting status code to {}", code);

        SelenideElement statusElement = getOperationRoot().$("#addResponseModal")
            .$$("drop-down").filter(attribute("id", "statusCodeDropDown")).first();
        SelenideElement definitionElement = getOperationRoot().$("#addResponseModal")
            .$$("drop-down").filter(attribute("id", "refDropDown")).first();

        statusElement.$("#statusCodeDropDown").click();
        statusElement.$(By.className("dropdown-menu")).$$("a").filter(text(code)).first().click();

        if (responseDef != null && !responseDef.isEmpty()) {
            definitionElement.$("#refDropDown").click();
            definitionElement.$(By.className("dropdown-menu")).$$("a").filter(text(responseDef)).first().click();
        }
        getButtonWithText("Add", getOperationRoot()).click();
    }

    public static void ensureMediaTypeExistsForResponse(String mediaType, String response) {
        selectResponse(response);
        if ($$("media-type-row").filter(Condition.text(mediaType)).size() == 0) {
            String buttonId = Buttons.MEDIA_TYPE.getButtonId();
            SelenideElement section = $("#addMediaTypeModal");
            $(By.linkText("Add a media type")).click();
            setDropDownValue(buttonId, mediaType, section);
            getButtonWithText("Add", section).click();
        }
    }

    public static void selectResponse(String code) {
        log.info("Selecting response {}", code);
        getOperationRoot().$(PathElements.RESPONSES_SECTION).$$(By.className("statusCode")).filter(text(code)).first().click();
    }

    public static void setValueInTextArea(String value, SelenideElement section) {
        log.info("Setting value {} into text area in section {}", value, section.getAttribute("class"));

        section.$$("div").filter(attribute("title", "Click to edit.")).first().click();

        section.$(By.cssSelector("ace-editor textarea")).sendKeys(Keys.CONTROL + "a");
        section.$(By.cssSelector("ace-editor textarea")).sendKeys(Keys.DELETE);
        section.$(By.cssSelector("ace-editor textarea")).sendKeys(value);

        Selenide.sleep(1000); // firefox needs at least second to process EXAMPLE

        getButtonWithTitle("Save changes.", section).click();
    }

    public static void setDropDownValue(String buttonId, String value, SelenideElement section) {
        log.info("Setting value {} in dropdown {} in section {}", value, buttonId, section.getAttribute("class"));
        section.$(buttonId).click();
        section.$$("li").filter(text(value)).first().click();
    }

    public static void setValueInLabel(String value, SelenideElement section, boolean isPencilLabel) {
        log.info("Setting value {} into label in section {}", value, section.getAttribute("class"));

        if (isPencilLabel) {
            section.$$("i").filter(attribute("title", "Click to edit.")).first().click();
        } else {
            section.$$("span").filter(attribute("title", "Click to edit.")).first().click();
        }
        getLabelWithType("text", section).setValue(value);
        getButtonWithTitle("Save changes.", section).click();
    }

    public static String getButtonId(String buttonName) {
        switch (buttonName) {
            case "type":
                return Buttons.PROPERTY_TYPE.getButtonId();
            case "of":
                return Buttons.PROPERTY_TYPE_OF.getButtonId();
            case "as":
                return Buttons.PROPERTY_TYPE_AS.getButtonId();
            case "required":
                return Buttons.PROPERTY_REQUIRED.getButtonId();
            case "media type":
                return Buttons.MEDIA_TYPE.getButtonId();
        }
        return null;
    }

    public static SelenideElement getDropdownMenuItem(String name) {
        return getAppRoot()
            .$$(By.cssSelector(".dropdown-menu li a span"))
            .filter(text(name))
            .shouldHave(CollectionCondition.size(1)).first();
    }

    /**
     * Opens collapsed section. If section is already opened do nothing.
     *
     * @param section which should be opened
     */
    public static void openCollapsedSection(SelenideElement page, By section) {
        ElementsCollection collapsedSection = page.$(section).$$("a").filter(attribute("class", "collapsed"));
        if (collapsedSection.size() > 0) {
            collapsedSection.first().click();
        }
    }
}
