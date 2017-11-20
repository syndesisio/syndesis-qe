package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class ActionConfigureComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-action-configure");
	}

	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	public boolean validate() {
		return getRootElement().is(visible);
	}

	public void fillInput(String inputId, String value) {
		SelenideElement input = this.getInputById(inputId);
		input.shouldBe(visible).clear();
		input.shouldBe(visible).sendKeys(value);
	}

	public void fillInput(SelenideElement element, String value) {
		element.shouldBe(visible).clear();
		element.shouldBe(visible).sendKeys(value);
	}
}
