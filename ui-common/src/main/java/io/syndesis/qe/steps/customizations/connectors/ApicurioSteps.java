package io.syndesis.qe.steps.customizations.connectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.SpecifySecurity;
import io.syndesis.qe.report.selector.ExcludeFromSelectorReports;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApicurioSteps {

    private static class Elements {
        public static final By SYNDESIS_ROOT = By.id("root");
        public static final By APICURIO_ROOT = By.name("apicurio-frame");
        //apicurio inner elements are not reachable from SYNDESYS_ROOT element (only APICURIO_ROOT is).
        //APICURIO_INNER_ROOT is root element for all apicurio inner elements:
        public static final By APICURIO_INNER_ROOT = By.className("api-editor");

        //apicurio gui elements
        public static By CARD_PF = By.className("pf-c-form");
        public static By WARNING_ICON = By.className("validation-icon");
        public static By PROBLEMS_CONTAINER = By.className("editor-problem-drawer");
        public static By OPERATIONS_CONTAINER = By.className("editor-outline");
        public static By PATH_SECTION = By.className("path-section");

        public static By VALIDATION_PROBLEM = By.className("drawer-pf-notification");
        public static By INFO_SECTION = By.cssSelector("operation-info-section");
        public static By ADD_OPERATION = By.cssSelector("button.icon-button");
        public static By OPERATION_KEBAB = By.id("dropdownKebab");
        public static By OPERATION = By.className("api-path");
        public static By OPERATION_KEBAB_MENU = By.className("detail-actions");
        public static By OPERATION_KEBAB_MENU_DELETE = By.xpath(".//span[contains(text(), \"Delete Path\")]");

        public static By RESPONSE_SECTION = By.className("responses-section");
        public static By MARKDOWN_EDITOR = By.className("inline-markdown-editor-label");

        //security elements
        public static By SECURITY_SECTION = By.className("security-section");
        public static By SECURITY_SCHEMA_ROW = By.cssSelector(".security-scheme");
        public static By BUTTON_ADD_SCHEME = By.cssSelector("button[title*='Add a security scheme']");
        public static By BUTTON_ADD_REQUIREMENT = By.cssSelector("button[title*='Add a security requirement']");

        //modal dialog elements
        public static By MODAL_DIALOG = By.className("modal-dialog");
        public static By MODAL_FOOTER = By.className("modal-footer");
        public static By MODAL_SUBMIT_ADD = By.xpath(".//button[contains(text(), \"Add\")]");
        public static By MODAL_PATH_INPUT = By.id("path");

        //syndesis apicurio-review page elements
        public static By WARNINGS = ByUtils.dataTestId("api-provider-review-actions-warning-number");
        public static By ERRORS = ByUtils.dataTestId("api-provider-review-actions-error-number");
        public static By PAGE_ROOT = By.className("pf-c-wizard__main");
        public static By NUMBER_OPERATIONS = ByUtils.dataTestId("api-provider-review-operations-number");
    }

    private static class TextFormElements {
        //apicurio text editing elements
        public static By INPUT_TEXT = By.cssSelector("input.form-control");
        public static By SUMMARY = By.className("summary");
        public static By SAVE = By.cssSelector("button[title*='Save changes.']");
    }

    private static class SecurityPageElements {
        public static By NAME = By.id("securitySchemeName");
        public static By SAVE = By.xpath(".//button[contains(text(), 'Save')]");
        public static By EDITOR = By.tagName("security-scheme-editor");
        public static By SECURITY_TYPE_DROPDOWN = By.className("dropdown-toggle");
        public static By SECURITY_DROPDOWN_MENU = By.className("dropdown-menu");
        public static By ACTION_HEADER = By.className("action-header");

        //security requirements page
        public static By SECURITY_REQUIREMENT = By.xpath(".//*[contains(.,'ImmovableName')]");
        public static By SECURITY_REQUIREMENT_ITEMS = By.className("list-group-item-heading");
    }

    @ExcludeFromSelectorReports
    @Then("^check that apicurio shows (\\d+) imported operations$")
    public void verifyOperations(int expectedCount) {
        SelenideElement operations = $(Elements.PAGE_ROOT).shouldBe(visible).find(Elements.NUMBER_OPERATIONS);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
            .containsIgnoringCase(Integer.toString(expectedCount))
            .containsIgnoringCase("operations");
    }

    @ExcludeFromSelectorReports
    @When("^check that apicurio imported operations number is loaded$")
    public void verifyOperationsAreVisible() {
        try {
            OpenShiftWaitUtils.waitFor(() -> !$(Elements.PAGE_ROOT).shouldBe(visible).find(Elements.NUMBER_OPERATIONS)
                .getText().equalsIgnoreCase("{{0}} operations"), 1000 * 60);
        } catch (InterruptedException | TimeoutException e) {
            fail("Operations number was not loaded in 60s.", e);
        }
    }

    @ExcludeFromSelectorReports
    @Then("^check that apicurio shows (\\d+) warnings$")
    public void verifyWarnings(int expectedCount) {
        SelenideElement operations = $(Elements.WARNINGS).shouldBe(visible);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
            .containsIgnoringCase(Integer.toString(expectedCount));
    }

    @ExcludeFromSelectorReports
    @Then("^check that apicurio shows (\\d+) errors?$")
    public void verifyErrors(int expectedCount) {
        SelenideElement operations = $(Elements.ERRORS).shouldBe(visible);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
            .containsIgnoringCase(Integer.toString(expectedCount));
    }

    @ExcludeFromSelectorReports
    @When("^remove warning via apicurio gui$")
    public void removeWarning() {
        $(Elements.WARNING_ICON).shouldBe(visible).click();
        //there isn't really a nice way how to wait as the box is there all the time, we are waiting for different items to show
        TestUtils.sleepForJenkinsDelayIfHigher(10);
        SelenideElement firstProblemElement = $(Elements.PROBLEMS_CONTAINER).shouldBe(visible)
            .$$(Elements.VALIDATION_PROBLEM).get(2);
        assertThat(firstProblemElement).isNotNull();
        assertThat(firstProblemElement.text()).containsIgnoringCase("Operation Summary should be less than 120 characters");

        try {
            firstProblemElement.shouldBe(visible).$(By.tagName("a")).shouldBe(visible).click();
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.PROBLEMS_CONTAINER).shouldBe(visible)
                .$$(Elements.VALIDATION_PROBLEM).get(0).shouldBe(visible).$(By.tagName("a")).shouldBe(visible).click();
        }

        $(Elements.WARNING_ICON).shouldBe(visible).click();
        $(Elements.INFO_SECTION).shouldBe(visible).$(TextFormElements.SUMMARY).shouldBe(visible).click();

        SelenideElement input = $(Elements.INFO_SECTION).$(TextFormElements.INPUT_TEXT).shouldBe(visible);

        try {
            input.clear();
            input.sendKeys("Short description");
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.INFO_SECTION).$(TextFormElements.INPUT_TEXT).shouldBe(visible).clear();
            $(Elements.INFO_SECTION).$(TextFormElements.INPUT_TEXT).shouldBe(visible)
                .sendKeys("Short description");
        }

        $(Elements.INFO_SECTION).$(TextFormElements.SAVE).shouldBe(visible).click();
        $(Elements.WARNING_ICON).shouldBe(visible).getText();
        assertThat($(Elements.WARNING_ICON).shouldBe(visible).getText()).containsIgnoringCase("57");
    }

    @When("^add an operation via apicurio gui$")
    public void addOperation() {
        doAddOperation(false);
    }

    @When("^add an operation with error via apicurio gui$")
    public void addOperationWithError() {
        doAddOperation(true);
    }

    /**
     * Add an operation for apicurito path.
     * If we want to see an error in review, we do not fill operation description
     *
     * @param withError - true to invoke an error in syndesis apicurito review due to empty description field
     */
    @ExcludeFromSelectorReports
    public void doAddOperation(boolean withError) {
        $(Elements.OPERATIONS_CONTAINER).shouldBe(visible).$(Elements.ADD_OPERATION).shouldBe(visible).click();

        SelenideElement pathInput = $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_PATH_INPUT).shouldBe(visible);

        try {
            pathInput.clear();
            pathInput.sendKeys("/syndesistestpath");
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_PATH_INPUT).shouldBe(visible).clear();
            $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_PATH_INPUT).shouldBe(visible)
                .sendKeys("/syndesistestpath");
        }
        $(Elements.MODAL_FOOTER).shouldBe(visible).$(Elements.MODAL_SUBMIT_ADD).shouldBe(visible).click();
        clickOnButtonInApicurio("Add Operation");

        $(Elements.RESPONSE_SECTION).shouldBe(visible).scrollIntoView(true)
            .$$(By.tagName("button")).filter(Condition.attribute("title", "Add a response to the operation."))
            .shouldHave(CollectionCondition.size(1)).first().click();

        $(Elements.MODAL_SUBMIT_ADD).shouldBe(visible).click();

        if (!withError) {
            $(Elements.RESPONSE_SECTION).shouldBe(visible).scrollIntoView(true)
                .$(Elements.MARKDOWN_EDITOR).shouldBe(visible)
                .click();

            executeJavaScript(
                "document.getElementsByTagName(\"ace-editor\").item(0).setAttribute(\"id\", \"editor\");" +
                    "ace.edit(\"editor\").setValue('description here');"
            );

            $(Elements.RESPONSE_SECTION).shouldBe(visible).scrollIntoView(true)
                .$$(By.tagName("button")).filter(Condition.attribute("title", "Save changes."))
                .shouldHave(CollectionCondition.size(1)).first().click();
        }
    }

    @ExcludeFromSelectorReports
    @When("^remove an operation via apicurio gui$")
    public void removeOperation() {
        $(Elements.PATH_SECTION).shouldBe(visible).$(Elements.OPERATION).shouldBe(visible).click();
        $(Elements.OPERATION_KEBAB).shouldBe(visible).click();
        SelenideElement kebabMenu = $(Elements.OPERATION_KEBAB_MENU);
        try {
            kebabMenu.$(Elements.OPERATION_KEBAB_MENU_DELETE).shouldBe(visible).click();
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.OPERATION_KEBAB_MENU)
                .$(Elements.OPERATION_KEBAB_MENU_DELETE).shouldBe(visible)
                .click();
        }
    }

    @ExcludeFromSelectorReports
    @When("^add security schema (BASIC|API Key|OAuth 2) via apicurio gui$")
    public void addSecuritySchema(String schemeType) {
        //child span element selected so the click is inside of the element
        $(Elements.SECURITY_SECTION).shouldBe(visible).$(By.tagName("span")).click();
        $(Elements.SECURITY_SECTION).shouldBe(visible).$(Elements.BUTTON_ADD_SCHEME).shouldBe(visible).click();

        SelenideElement nameInput = $(SecurityPageElements.NAME).shouldBe(visible);

        try {
            nameInput.sendKeys("ImmovableName");
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(SecurityPageElements.NAME).shouldBe(visible)
                .sendKeys("ImmovableName");
        }

        //select security option
        $(SecurityPageElements.EDITOR).shouldBe(visible).$(SecurityPageElements.SECURITY_TYPE_DROPDOWN).shouldBe(visible).click();
        $(SecurityPageElements.EDITOR).shouldBe(visible).$(SecurityPageElements.SECURITY_DROPDOWN_MENU).shouldBe(visible)
            .$(By.xpath(".//a[contains(text(), \"" + schemeType + "\")]")).shouldBe(visible).click();

        if ("API Key".equalsIgnoreCase(schemeType)) {
            $(By.className("apiKey-auth")).find(By.id("in20")).click();
            $(By.className("apiKey-auth")).findAll("li").filter(text("HTTP header")).first().click();
            $(By.id("name20")).sendKeys("headerName");
        }

        if ("OAuth 2".equalsIgnoreCase(schemeType)) {
            $(By.id("flow")).shouldBe(visible).click();
            $(By.id("flow")).shouldBe(visible).parent().$(By.xpath(".//a[contains(text(), \"Access Code\")]")).shouldBe(visible).click();
            $(By.id("authorizationUrl")).sendKeys("http://syndesis.io");
            $(By.id("tokenUrl")).sendKeys("https://hihi.com");
        }

        ElementsCollection saveButtons = $(SecurityPageElements.ACTION_HEADER).shouldBe(visible).$$(SecurityPageElements.SAVE);
        assertThat(saveButtons.size()).isEqualTo(1);
        saveButtons.get(0).shouldBe(visible).click();

        $(Elements.BUTTON_ADD_REQUIREMENT).shouldBe(visible).click();

        try {
            OpenShiftWaitUtils.waitFor(() -> $(SecurityPageElements.SECURITY_REQUIREMENT).exists(), 60 * 1000L);
        } catch (InterruptedException | TimeoutException e) {
            fail("Security requirement was not found.");
        }

        ElementsCollection items = $$(SecurityPageElements.SECURITY_REQUIREMENT_ITEMS)
            .shouldBe(sizeGreaterThanOrEqual(1)).filterBy(text("ImmovableName"));
        assertThat(items).hasSize(1);
        items.first().click();

        saveButtons = $(SecurityPageElements.ACTION_HEADER).shouldBe(visible).$$(SecurityPageElements.SAVE);
        assertThat(saveButtons.size()).isEqualTo(1);
        saveButtons.get(0).shouldBe(visible).click();
    }

    @ExcludeFromSelectorReports
    @Then("^check that api connector authentication section contains security type \"([^\"]*)\"$")
    public void verifySelectedSecurity(String securityType) {
        SelenideElement securityForm = $(SpecifySecurity.Element.SECURITY_FORM).shouldBe(visible);
        switch (securityType) {
            case "OAuth 2.0":
                securityForm.$(SpecifySecurity.SecurityType.OAUTH_2).shouldBe(visible);
                assertThat(securityForm.text()).containsIgnoringCase("OAuth 2.0 ");
                break;
            case "HTTP Basic Authentication":
                securityForm.$(SpecifySecurity.SecurityType.HTTP_BASIC_AUTHENTICATION).shouldBe(visible);
                assertThat(securityForm.text()).containsIgnoringCase("HTTP Basic Authentication");
                break;
            case "API Key":
                securityForm.$(SpecifySecurity.SecurityType.API_KEY).shouldBe(visible);
                assertThat(securityForm.text()).containsIgnoringCase("API Key");
                break;
            case "No Security":
                securityForm.$(SpecifySecurity.SecurityType.NO_SECURITY).shouldBe(visible);
                assertThat(securityForm.text()).containsIgnoringCase("No Security");
                break;
            default:
                fail("The Auth type < " + securityType + "> is not implemented by the test.");
        }
    }

    @ExcludeFromSelectorReports
    @When("^click on button \"([^\"]*)\" while in apicurio studio page$")
    public void clickOnButtonInApicurio(String buttonTitle) {
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        getApicurioButton(buttonTitle).shouldBe(visible, enabled).shouldNotHave(attribute("disabled")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }

    @When("^change frame to \"([^\"]*)\"$")
    public void changeFrameTo(String frame) {
        if ("apicurio".equals(frame)) {
            switchTo().frame("apicurio-frame");
        } else if ("syndesis".equals(frame)) {
            switchTo().parentFrame();
        }
        log.info("FRAME CHANGED TO *{}*,", checkFrame());
    }

    private String checkFrame() {
        return Selenide.executeJavaScript("return self.name");
    }

    public SelenideElement getButton(String buttonTitle) {
        log.info("searching for button {}", buttonTitle);
        return $(Elements.SYNDESIS_ROOT).shouldBe(visible).findAll(By.tagName("button"))
            .filter(Condition.matchText("(\\s*)" + buttonTitle + "(\\s*)")).shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    @When("^click on the \"([^\"]*)\" apicurio button*$")
    public void clickOnApicurioButton(String buttonTitle) {
        SelenideElement button = getApicurioButton(buttonTitle);
        log.info("apicurio button found *{}*", button.toString());
        button.shouldBe(visible, enabled).shouldNotHave(attribute("disabled")).click();
    }

    @Then("^check that apicurio connection authentication type contains only fields:$")
    public void checkFieldsExistence(DataTable fields) {
        List<List<String>> dataRows = fields.cells();
        List<String> first = new ArrayList<String>();
        for (List<String> row : dataRows) {
            first.add(row.get(0));
        }
        new Form($(Elements.CARD_PF)).checkByTestId(first);
    }

    private SelenideElement getApicurioButton(String buttonTitle) {
        log.info("searching for apicurio button *{}*", buttonTitle);
        return $(Elements.APICURIO_INNER_ROOT).shouldBe(visible).findAll(By.tagName("button"))
            .filter(Condition.matchText("(\\s*)" + buttonTitle + "(\\s*)")).shouldHave(sizeGreaterThanOrEqual(1)).first();
    }

    @When("configure the {string} security schema")
    public void configureSecuritySchema(String name) {
        SelenideElement securityRow = $(Elements.SECURITY_SECTION).$$(Elements.SECURITY_SCHEMA_ROW).find(Condition.text(name));
        if (securityRow == null) {
            fail("Couldn't locate security schema {}, double check the spelling. Make sure you are using only the schema name, not also the type",
                name);
        }
        securityRow.$(".dropdown-toggle").click();
        securityRow.$(".dropdown-menu").$$("li").find(Condition.text("Edit")).click();
    }
}
