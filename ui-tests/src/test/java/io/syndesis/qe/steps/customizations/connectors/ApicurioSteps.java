package io.syndesis.qe.steps.customizations.connectors;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@Slf4j
public class ApicurioSteps {

    @Autowired
    private CommonSteps commonSteps;

    private static class Elements {
        public static final By SYNDESIS_ROOT = By.cssSelector("syndesis-root");

        //apicurio gui elements
        public static By WARNING_ICON = By.className("validation-icon");
        public static By PROBLEMS_CONTAINER = By.className("editor-validation");
        public static By OPERATIONS_CONTAINER = By.className("editor-outline");
        public static By PATH_SECTION = By.id("path-section-body");

        public static By VALIDATION_PROBLEM = By.className("validation-problem");
        public static By INFO_SECTION = By.id("info-section-body");
        public static By ADD_OPERATION = By.cssSelector("button.icon-button");
        public static By OPERATION_KEBAB = By.id("dropdownKebab");
        public static By OPERATION = By.className("api-path");
        public static By OPERATION_KEBAB_MENU = By.className("dropdown-menu");
        public static By OPERATION_KEBAB_MENU_DELETE = By.xpath("//span[contains(text(), \"Delete Path\")]");

        //security elements
        public static By SECURITY_SECTION = By.className("security-section");
        public static By SECURITY_REQUIREMENTS_SECTION = By.className("security-requirements-section");
        public static By BUTTON_ADD_SCHEME = By.xpath("//button[contains(text(), \"Add Scheme\")]");
        public static By BUTTON_ADD_REQUIREMENT = By.xpath("//button[contains(text(), \"Add Requirement\")]");


        //modal dialog elements
        public static By MODAL_DIALOG = By.className("modal-dialog");
        public static By MODAL_FOOTER = By.className("modal-footer");
        public static By MODAL_SUBMIT_ADD = By.xpath("//button[contains(text(), \"Add\")]");
        public static By MODAL_SUBMIT_OK = By.xpath("//button[contains(text(), \"OK\")]");
        public static By MODAL_PATH_INPUT = By.id("path");
        public static By MODAL_SCHEME_NAME_INPUT = By.id("schemeName");
        public static By MODAL_TYPE_INPUT = By.id("type");
        public static By MODAL_DROPDOWN_MENU = By.className("dropdown-menu");
        public static By MODAL_SECURITY_CHECKBOX = By.className("list-view-pf-checkbox");

        //syndesis apicurio-review page elements
        public static By WARNINGS = By.className("connector-actions__label--warning");
        public static By ERRORS = By.className("connector-actions__label--error");
        public static By OPERATIONS = By.className("connector-actions");
        public static By OPERATION_ITEM = By.tagName("li");

        //specify security elements
        public static By AUTHENTICATION_CONTAINER = By.tagName("syndesis-api-connector-auth");

    }

    private static class TextFormElements {
        //apicurio text editing elements
        public static By TEXT_EDITOR = By.tagName("inline-text-editor");
        public static By INPUT_TEXT = By.cssSelector("input.form-control");
        public static By EDIT = By.className("overlay-action");
        public static By SAVE = By.cssSelector("button[type*='submit']");
    }

    @Then("^check that apicurio shows (\\d+) imported operations$")
    public void verifyOperations(int expectedCount) {
        SelenideElement operations = $(Elements.OPERATIONS).shouldBe(visible).$$(Elements.OPERATION_ITEM).get(0);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
                .containsIgnoringCase(Integer.toString(expectedCount))
                .containsIgnoringCase("operations");
    }

    @When("^check that apicurio imported operations number is loaded$")
    public void verifyOperationsAreVisible() {
        try {
            OpenShiftWaitUtils.waitFor(() -> !$(Elements.OPERATIONS).shouldBe(visible).$$(Elements.OPERATION_ITEM).get(0)
                    .getText().equalsIgnoreCase("{{0}} operations"), 1000 * 60);
        } catch (InterruptedException | TimeoutException e) {
            fail("Operations number was not loaded in 60s.", e);
        }
    }

    @Then("^check that apicurio shows (\\d+) warnings$")
    public void verifyWarnings(int expectedCount) {
        SelenideElement operations = $(Elements.WARNINGS).shouldBe(visible);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
                .containsIgnoringCase(Integer.toString(expectedCount));
    }

    @Then("^check that apicurio shows (\\d+) errors?$")
    public void verifyErrors(int expectedCount) {
        SelenideElement operations = $(Elements.ERRORS).shouldBe(visible);
        assertThat(operations).isNotNull();
        assertThat(operations.getText())
                .containsIgnoringCase(Integer.toString(expectedCount));
    }

    @When("^remove warning via apicurio gui$")
    public void removeWarning() {
        $(Elements.WARNING_ICON).shouldBe(visible).click();
        //there isn't really a nice way how to wait as the box is there all the time, we are waiting for different items to show
        TestUtils.sleepForJenkinsDelayIfHigher(10);
        SelenideElement firstProblemElement = $(Elements.PROBLEMS_CONTAINER).shouldBe(visible)
                .$$(Elements.VALIDATION_PROBLEM).get(0);
        assertThat(firstProblemElement).isNotNull();
        assertThat(firstProblemElement.text()).containsIgnoringCase("OP-001");

        try {
            firstProblemElement.shouldBe(visible).click();
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.PROBLEMS_CONTAINER).shouldBe(visible)
                    .$$(Elements.VALIDATION_PROBLEM).get(0).shouldBe(visible).click();
        }

        clickOnButtonInApicurio("Go To Problem");
        $(Elements.INFO_SECTION).shouldBe(visible).$(TextFormElements.TEXT_EDITOR).shouldBe(visible).hover();
        $(TextFormElements.EDIT).shouldBe(visible).click();

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

        assertThat($(Elements.WARNING_ICON).shouldBe(visible).getText()).containsIgnoringCase("57");
    }

    @When("^add an operation via apicurio gui$")
    public void addOperation() {
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
        clickOnButtonInApicurio("Create Operation");
    }

    @When("^remove an operation via apicurio gui$")
    public void removeOperation() {
        $(Elements.PATH_SECTION).shouldBe(visible).$(Elements.OPERATION).shouldBe(visible).click();
        $(Elements.OPERATION_KEBAB).shouldBe(visible).click();
        SelenideElement kebabMenu = $$(Elements.OPERATION_KEBAB_MENU).shouldHaveSize(2).get(1);
        try {
            kebabMenu.$(Elements.OPERATION_KEBAB_MENU_DELETE).shouldBe(visible).click();
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $$(Elements.OPERATION_KEBAB_MENU).shouldHaveSize(2).get(1)
                    .$(Elements.OPERATION_KEBAB_MENU_DELETE).shouldBe(visible)
                    .click();
        }
    }

    @When("^add security schema (BASIC|API Key|OAuth 2) via apicurio gui$")
    public void addSecuritySchema(String schemeType) {
        //child span element selected so the click is inside of the element
        $(Elements.SECURITY_SECTION).shouldBe(visible).$(By.tagName("span")).click();
        $(Elements.SECURITY_SECTION).shouldBe(visible).$(Elements.BUTTON_ADD_SCHEME).shouldBe(visible).click();

        SelenideElement nameInput = $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_SCHEME_NAME_INPUT).shouldBe(visible);

        try {
            nameInput.clear();
            nameInput.sendKeys("ImmovableName");
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_SCHEME_NAME_INPUT).shouldBe(visible).clear();
            $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_SCHEME_NAME_INPUT).shouldBe(visible)
                    .sendKeys("ImmovableName");
        }

        //select security option
        $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_TYPE_INPUT).shouldBe(visible).click();
        $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_DROPDOWN_MENU).shouldBe(visible)
                .$(By.xpath("//a[contains(text(), \"" + schemeType + "\")]")).shouldBe(visible).click();

        if (schemeType.equalsIgnoreCase("API Key")) {
            $(By.id("name")).sendKeys("headerName");
        }

        if (schemeType.equalsIgnoreCase("OAuth 2")) {
            $(By.id("authorizationUrl")).sendKeys("https://hihi");
        }

        //confirm modal window
        $(Elements.MODAL_FOOTER).shouldBe(visible).$(Elements.MODAL_SUBMIT_OK).shouldBe(visible).click();
        $(Elements.SECURITY_REQUIREMENTS_SECTION).shouldBe(visible).scrollTo();

        //child span element selected so the click is inside of the element
        $(Elements.SECURITY_REQUIREMENTS_SECTION).shouldBe(visible).$(By.tagName("span")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(5);

        $(Elements.SECURITY_REQUIREMENTS_SECTION).shouldBe(visible).$(Elements.BUTTON_ADD_REQUIREMENT).shouldBe(visible).click();
        $(Elements.MODAL_DIALOG).shouldBe(visible).$(Elements.MODAL_SECURITY_CHECKBOX).shouldBe(visible).click();
        $(Elements.MODAL_FOOTER).shouldBe(visible).$(Elements.MODAL_SUBMIT_OK).shouldBe(visible).click();
    }

    @Then("^check that api connector authentication section contains text \"([^\"]*)\"$")
    public void verifySelectedSecurity(String expectedText) {
        assertThat($(Elements.AUTHENTICATION_CONTAINER).shouldBe(visible).text())
                .containsIgnoringCase(expectedText);
    }

    @When("^click on button \"([^\"]*)\" while in apicurio studio page$")
    public void clickOnButtonInApicurio(String buttonTitle) {
        TestUtils.sleepForJenkinsDelayIfHigher(2);
        getButton(buttonTitle).shouldBe(visible, enabled).shouldNotHave(attribute("disabled")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }

    public SelenideElement getButton(String buttonTitle) {
        log.info("searching for button {}", buttonTitle);
        return $(Elements.SYNDESIS_ROOT).shouldBe(visible).findAll(By.tagName("button"))
                .filter(Condition.matchText("(\\s*)" + buttonTitle + "(\\s*)")).shouldHave(sizeGreaterThanOrEqual(1)).first();
    }
}
