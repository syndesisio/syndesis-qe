package io.syndesis.qe.pages.integrations.edit;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class FlowConnection {

	private static final class Element {
		public static final By ACTIVE = By.cssSelector("p.icon.active");
	}

	private String type;
	private SelenideElement element;

	public FlowConnection(String type, SelenideElement element) {
		this.type = type;
		this.element = element;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SelenideElement getElement() {
		return element;
	}

	public void setElement(SelenideElement element) {
		this.element = element;
	}

	/**
	 * Check if this element is active
	 *
	 * @returns boolean
	 */
	public boolean isActive() {
		return this.element.find(Element.ACTIVE).isDisplayed();
	}
}
