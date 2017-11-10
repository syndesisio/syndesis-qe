package io.syndesis.qe.pages;

import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

import com.codeborne.selenide.SelenideElement;

public class SyndesisRootPage {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-root");
	}

	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	public boolean validate() {
		return getRootElement().is(visible);
	}
}