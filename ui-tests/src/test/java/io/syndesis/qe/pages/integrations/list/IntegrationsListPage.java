package io.syndesis.qe.pages.integrations.list;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class IntegrationsListPage extends SyndesisPageObject {
	
	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-list-page");
	}
	
	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	public boolean validate() {
		return getRootElement().is(visible);
	}

	public IntegrationsListComponent listComponent() {
		return new IntegrationsListComponent();
	}
}
