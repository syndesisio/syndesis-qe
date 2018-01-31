package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class SyndesisRootPage extends SyndesisPageObject {

	private static final class Link {
		public static final By HOME = By.cssSelector("a.navbar-brand");
	}

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

	public void goHome() {
		this.getRootElement().find(Link.HOME).shouldBe(visible).click();
	}
}
