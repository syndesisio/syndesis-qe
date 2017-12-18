package io.syndesis.qe.pages.customizations.api_client_connectors.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.SelenideElement;

import java.io.File;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.java_interfaces.wizard.WizardSucceedable;

public class UploadSwagger extends SyndesisPageObject implements WizardSucceedable {

	private static class Button {
		public static By NEXT = By.xpath("//button[contains(.,'Next')]");
	}

	private static class Element {
		public static By ROOT = By.cssSelector("syndesis-api-connector-swagger-upload");
	}

	private static class Input {
		public static By CHOOSE_FILE = By.xpath("//input[@type='file']");
		public static By URL = By.name("swaggerFileUrl");
		public static By UPLOAD_A_SWAGGER_FILE = By.xpath("//input[@type='radio' and ../text()[contains(.,'Upload a Swagger file')]]");
		public static By USE_A_URL = By.xpath("//input[@type='radio' and ../text()[contains(.,'Use a URL')]]");
	}

	@Override
	public void nextWizardStep() {
		$(Button.NEXT).shouldBe(visible).click();
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).should(exist);
	}

	@Override
	public boolean validate() {
		try {
			getRootElement();
			return true;
		} catch (WebDriverException wde) {
			return false;
		}
	}

	public void upload(String source, String url) {
		switch (source) {
			case "file":
				uploadFileFromPath(url);
				break;
			case "url":
				uploadFileFromUrl(url);
				break;
			default:
				break;
		}
	}

	public void uploadFileFromPath(String path) {
		$(Input.UPLOAD_A_SWAGGER_FILE).shouldBe(visible).click();
		$(Input.CHOOSE_FILE).shouldBe(visible).uploadFile(new File(getClass().getClassLoader().getResource(path).getFile()));
	}

	public void uploadFileFromUrl(String url) {
		$(Input.USE_A_URL).shouldBe(visible).click();
		$(Input.URL).shouldBe(visible).sendKeys(url);
	}
}
