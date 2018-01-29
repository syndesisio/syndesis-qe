package io.syndesis.qe.pages.customizations.connectors.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.interfaces.wizard.WizardStep;

public class Security extends SyndesisPageObject implements WizardStep {

	private static class Button {
		public static By NEXT = By.xpath("//button[contains(.,'Next')]");
	}

	private static class Element {
		public static By ROOT = By.cssSelector("syndesis-api-connector-auth");
	}

	private static class Input {
		public static By AUTHORIZATION_URL = By.xpath("//input[@formcontrolname='tokenEndpoint']");
	}

	@Override
	public void goToNextWizardStep() {
		$(Button.NEXT).shouldBe(visible).click();
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).should(exist);
	}

	@Override
	public boolean validate() {
		return getRootElement().exists();
	}

	public void setUpOAuth2Security(String authorizationUrl) {
		$(Input.AUTHORIZATION_URL).setValue(authorizationUrl);
	}
}
