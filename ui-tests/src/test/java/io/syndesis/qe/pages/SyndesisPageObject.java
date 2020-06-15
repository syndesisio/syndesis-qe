package io.syndesis.qe.pages;

import static org.junit.Assert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.is;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import io.syndesis.qe.utils.Alert;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.Conditions;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.junit.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SyndesisPageObject {

    public abstract SelenideElement getRootElement();

    public abstract boolean validate();

    public SelenideElement getButton(String buttonTitle) {
        return getButton(buttonTitle, getRootElement());
    }

    public SelenideElement getButtonByCssClassName(String buttonClassName) {
        return getButtonByCssClassName(buttonClassName, getRootElement());
    }

    public SelenideElement getButtonByCssClassName(String buttonClassName, SelenideElement differentRoot) {
        log.info("searching for button *{}*", buttonClassName);

        waitForButtons(Condition.cssClass(buttonClassName), differentRoot);

        ElementsCollection foundButtons = differentRoot.shouldBe(visible).findAll(By.className(buttonClassName)).filter(visible);
        return foundButtons.get(0);
    }

    public SelenideElement getButton(String buttonTitle, SelenideElement differentRoot) {
        log.info("searching for button *{}*", buttonTitle);

        waitForButtons(Condition.exactText(buttonTitle), differentRoot);

        ElementsCollection foundButtons = differentRoot.shouldBe(visible).findAll(By.tagName("button"))
            .filter(Condition.exactText(buttonTitle))
            .exclude(Conditions.STALE_ELEMENT)
            .shouldHave(sizeGreaterThanOrEqual(1));

        if (foundButtons.size() > 1) {
            log.warn("Ambiguous button title. Found more that 1 button with title {}", buttonTitle);
        }
        log.info("Button found! ");

        return foundButtons.first();
    }

    private void waitForButtons(Condition condition, SelenideElement differentRoot) {
        int maxRetries = 60;
        int retries = 0;
        boolean found = false;
        BooleanSupplier bs = () -> differentRoot.shouldBe(visible).findAll(By.tagName("button")).filter(condition).size() >= 1;
        while (!found && retries < maxRetries) {
            try {
                found = bs.getAsBoolean();
            } catch (StaleElementReferenceException ex) {
                log.warn("Element was detached from the page, trying again to find a button but now within syndesis-root element");
                bs = () -> $(By.id("root")).shouldBe(visible).findAll(By.tagName("button")).filter(condition).size() >= 1;
            }
            if (!found) {
                retries++;
                TestUtils.sleepIgnoreInterrupt(1000L);
            }
        }
    }

    public SelenideElement getFirstVisibleButton(String buttonTitle) {
        log.info("searching for first visible button {}", buttonTitle);
        ElementsCollection buttonElements = this.getRootElement().findAll(By.linkText(buttonTitle)).filter(visible);
        return buttonElements.get(0);
    }

    /**
     * Fill form with given data. It will look for ui element for every map entry.
     *
     * @param data key,value data. Key is used for element lookup
     * @param parrentElement search inputs in child elements of this one
     */
    public void fillForm(Map<By, String> data, SelenideElement parrentElement) {
        String value;
        for (By locator : data.keySet()) {
            value = data.get(locator);
            SelenideElement inputElement = parrentElement.find(locator).shouldBe(visible);
            if ("select".equalsIgnoreCase(inputElement.getTagName())) {
                inputElement.selectOption(value);
            } else {
                inputElement.sendKeys(value);
            }
        }
    }

    public void selectFromDropDown(String selectId, String option) {
        SelenideElement select = this.getElementById(selectId).shouldBe(visible);
        assertThat(select.getTagName(), is("select"));
        select.selectOption(option);
    }

    public String getCurrentUrl() {
        return getWebDriver().getCurrentUrl();
    }

    public void goToUrl(String url) {
        getWebDriver().get(url);
    }

    public SelenideElement getLink(String linkTitle) {
        return this.getRootElement().find(By.linkText(linkTitle));
    }

    public SelenideElement getLink(String linkTitle, int index) {
        ElementsCollection links = this.getLinks(linkTitle);
        return links.get(index);
    }

    public ElementsCollection getLinks(String linkTitle) {
        return this.getRootElement().findAll(By.linkText(linkTitle));
    }

    public SelenideElement getLinkRandom(String linkTitle) {
        ElementsCollection links = this.getLinks(linkTitle);
        int index = (int) Math.floor(Math.random() * links.size());
        return links.get(index);
    }

    public SelenideElement getElementByCssSelector(String cssSelector) {
        return this.getRootElement().find(By.cssSelector(cssSelector));
    }

    public SelenideElement getElementByXpath(String xpathSelector) {
        return this.getRootElement().find(By.xpath(xpathSelector));
    }

    public SelenideElement getElementByClassName(String elementClassName) {
        return this.getRootElement().find(By.className(elementClassName));
    }

    public SelenideElement getElementByLocator(By elementLocator) {
        log.info("searching for element {}", elementLocator.toString());
        return this.getRootElement().$(elementLocator);
    }

    public SelenideElement getTitleByText(String text) {
        log.info("searching for title {}", text);
        return this.getRootElement().find(By.cssSelector(String.format("h2:contains('%s')", text)));
    }

    public ElementsCollection getElementsByClassName(String elementClassName) {
        return this.getRootElement().findAll(By.className(elementClassName));
    }

    public SelenideElement getElementRandom(String elementClassName) {
        ElementsCollection elements = this.getElementsByClassName(elementClassName);
        int index = (int) Math.floor(Math.random() * elements.size());
        return elements.get(index);
    }

    public SelenideElement getElementRandom(By locator) {
        ElementsCollection elements = this.getRootElement().findAll(locator);
        int index = (int) Math.floor(Math.random() * elements.size());
        return elements.get(index);
    }

    public void selectOption(SelenideElement selectElement, String option) {
        SelenideElement optionElement = getElementContainingText(By.tagName("option"), option, selectElement);
        optionElement.shouldBe(visible).click();
    }

    private SelenideElement getElementById(String inputId) {
        return this.getRootElement().find(By.id(inputId));
    }

    private SelenideElement getElementByDataTestid(String inputDataTestid) {
        return this.getRootElement().find(ByUtils.dataTestId(inputDataTestid));
    }

    public SelenideElement getInputBySelector(String selector) {
        return this.getRootElement().find(By.cssSelector(selector));
    }

    public void fillInputById(String inputId, String value) {
        SelenideElement input = this.getElementById(inputId);
        this.doFillInput(input, value);
    }

    public void fillInputByDataTestid(String dataTestid, String value) {
        SelenideElement input = this.getElementByDataTestid(dataTestid);
        this.doFillInput(input, value);
    }

    public void selectValueByDataTestid(String dataTestid, String value) {
        SelenideElement input = this.getElementByDataTestid(dataTestid);
        this.setElementValue(value, input);
    }

    private void doFillInput(SelenideElement input, String value) {
        assertThat(input.getTagName(), is("input"));
        if (input.getAttribute("type").equals("checkbox")) {
            input.setSelected(Boolean.valueOf(value));
        } else {
            input.shouldBe(visible).clear();
            input.shouldBe(visible).sendKeys(value);
        }
    }

    public void fillInput(SelenideElement input, String value) {
        if (input.getAttribute("type").equals("checkbox")) {
            input.setSelected(Boolean.valueOf(value));
        } else {
            input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
            input.sendKeys(Keys.BACK_SPACE);
            input.shouldBe(visible).clear();
            input.shouldBe(visible).sendKeys(value);
        }
    }

    public void setElementValue(String value, SelenideElement element) {
        switch (element.getTagName()) {
            case "input":
                fillInput(element, value);
                break;
            case "select":
                element.selectOption(value);
                break;
        }
    }

    public SelenideElement getElementContainingText(By by, String text) {
        return getElementContainingText(by, text, getRootElement());
    }

    public SelenideElement getElementContainingText(By by, String text, SelenideElement differentRoot) {
        try {
            OpenShiftWaitUtils.waitFor(() -> differentRoot.shouldBe(visible).findAll(by).size() > 0, 30 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Element was not found in 30s", e);
        }

        ElementsCollection elements = differentRoot.shouldBe(visible).findAll(by).shouldBe(sizeGreaterThan(0));
        log.debug("Found " + elements.size() + " elements with selector, filtering...");

        elements = elements.filter(exactText(text));
        assertThat(elements.size()).isGreaterThanOrEqualTo(1);
        return elements.first();
    }

    public String getElementText(By locator) {
        SelenideElement element = this.getRootElement().shouldBe(visible).find(locator);
        return element.shouldBe(visible).getText();
    }

    public void checkButtonStatus(String buttonTitle, String status) {
        log.info("checking button {} status", buttonTitle);
        // this speed-up the check we don't need shoulBe here
        this.getButton(buttonTitle).waitUntil(this.conditionValueOf(status), 5 * 1000).is(this.conditionValueOf(status));
    }

    public Condition conditionValueOf(String status) {
        Condition condition;
        switch (status.toLowerCase()) {
            case "active":
            case "visible":
                condition = visible;
                break;
            case "disabled":
            case "inactive":
                condition = disabled;
                break;
            default:
                log.error("Invalid status {} supplied, please check your feature files for typos", status);
                Assert.fail("Invalid status " + status + " supplied, please check your feature files for typos");
                condition = visible;
                break;
        }
        return condition;
    }

    public void removeAllAlertsFromPage(Alert alertOption) {
        ElementsCollection alerts = getCloseableAllerts(alertOption);
        while (!alerts.isEmpty()) {
            for (SelenideElement alert : alerts) {
                SelenideElement button = alert.$(Alert.Element.CLOSE_BUTTON);
                if (alert.isDisplayed()) {
                    //only if the alert still exists and it is not stale
                    button.click();
                }
            }
            alerts = getCloseableAllerts(alertOption);
        }
    }

    public ElementsCollection getCloseableAllerts(Alert alert) {
        return $$(alert.getBy()).exclude(Conditions.WO_CLOSE_BUTTONS);
    }
}
