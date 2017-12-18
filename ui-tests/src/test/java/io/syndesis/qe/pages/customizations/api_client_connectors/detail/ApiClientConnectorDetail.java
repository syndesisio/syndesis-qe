package io.syndesis.qe.pages.customizations.api_client_connectors.detail;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class ApiClientConnectorDetail extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-api-connector-detail");
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).should(Condition.exist);
	}

	@Override
	public boolean validate() {
		try {
			$(Element.ROOT).should(exist);
			return true;
		} catch (Throwable t) {
			System.out.println("THROWABLE: " + t.getClass().getName());
			return false;
		}
	}

	public void edit(SelenideElement editableText) {
		editableText.shouldBe(visible).click();
	}

	public SelenideElement getEditablePropertyLabel(String propertyName) {
		return $(By.xpath("//label[text()='" + propertyName + "']")).shouldBe(visible);
	}

	public SelenideElement getTextToEditElement(String propertyName) {
		return getEditablePropertyLabel(propertyName).$(By.xpath("./../span"));
	}

	public SelenideElement getTextEditor(String id) {
		return $(byId(id)).shouldBe(visible);
	}
}
