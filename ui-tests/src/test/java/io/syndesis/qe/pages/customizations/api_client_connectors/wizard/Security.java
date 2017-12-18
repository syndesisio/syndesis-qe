package io.syndesis.qe.pages.customizations.api_client_connectors.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.java_interfaces.wizard.WizardSucceedable;

public class Security extends SyndesisPageObject implements WizardSucceedable {

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
	public void nextWizardStep() {
		$(Button.NEXT).shouldBe(visible).click();
	}

	@Override
	public SelenideElement getRootElement() {
		return null;
	}

	@Override
	public boolean validate() {
		try {
			$(Element.ROOT).should(exist);
			return true;
		} catch (WebDriverException wde) {
			return false;
		}
	}

	public void setUpSecurity(String authorizationUrl) {
		$(Input.AUTHORIZATION_URL).setValue(authorizationUrl);
	}
}
