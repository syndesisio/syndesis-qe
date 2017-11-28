package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

/**
 * Created by sveres on 11/29/17.
 */
public class IntegrationSaveOrAddStepComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-save-or-add-step");
		public static final By TITLE = By.cssSelector("h1[innertext='Add to Integration']");
	}
	@Override
	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	@Override
	public boolean validate() {
		return this.getRootElement().find(Element.TITLE).is(visible);
	}
}
