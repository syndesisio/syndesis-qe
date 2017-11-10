package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public abstract class SyndesisPageObject {
	
	private static final class Link {
		public static final By HOME = By.cssSelector("a.navbar-brand");
	}

	abstract SelenideElement getRootElement();

	abstract boolean validate();

	public String getCurrentUrl() {
		return getWebDriver().getCurrentUrl();
	}

	public void goToUrl(String url) {
		getWebDriver().get(url);
	}

	public void goHome() {
		this.getRootElement().find(Link.HOME).shouldBe(visible).click();
	}

	public SelenideElement getLink(String linkTitle) {
		return this.getRootElement().find(By.linkText(linkTitle));
	}

	public ElementsCollection getLinks(String linkTitle) {
		return this.getRootElement().findAll(By.linkText(linkTitle));
	}

	public void clickLink(String linkTitle) {
		this.getLink(linkTitle).shouldBe(visible).click();	
	}

	public void clickLinkRandom(String linkTitle) {
		ElementsCollection links = this.getLinks(linkTitle);
		int index = (int) Math.floor(Math.random() * links.size());
		links.get(index).shouldBe(visible).click();
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

	public ElementsCollection getElementsByClassName(String elementClassName) {
		return this.getRootElement().findAll(By.className(elementClassName));
	}

	public void clickElementRandom(String elementClassName) {
		ElementsCollection elements = this.getElementsByClassName(elementClassName);
		int index = (int) Math.floor(Math.random() * elements.size());
		elements.get(index).shouldBe(visible).click();
	}

	public void selectOption(SelenideElement selectElement, String option) {
		ElementsCollection optionElements = selectElement.findAll(By.tagName("option"));
		optionElements = optionElements.filter(Condition.exactText(option));
		SelenideElement optionElement = optionElements.shouldBe(sizeGreaterThan(0)).first();
		optionElement.shouldBe(visible).click();
	}
}
