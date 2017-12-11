package io.syndesis.qe.pages.customizations;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsListComponent;
import lombok.Getter;

@Getter
public class CustomizationsPage extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-customizations");
	}

	private static final class Link {
		public static final By TECH_EXTENSIONS = By.linkText("Technical Extensions");
		public static final By API_CLIENT_CONNECTORS = By.linkText("Technical Extensions");
	}

	private TechExtensionsListComponent techExtensionsListComponent = new TechExtensionsListComponent();

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		SelenideElement rootElement = this.getRootElement();
		
		boolean isTechExtensionsLinkPresent = rootElement.find(Link.TECH_EXTENSIONS).is(visible);
		boolean isApiClientLinkPresent = rootElement.find(Link.API_CLIENT_CONNECTORS).is(visible);
		
		return isTechExtensionsLinkPresent && isApiClientLinkPresent;
	}
	
	public void goToTechExtensions() {
		this.getRootElement().find(Link.TECH_EXTENSIONS).shouldBe(visible).click();
	}

	public void goToApiClientConnectors() {
		this.getRootElement().find(Link.API_CLIENT_CONNECTORS).shouldBe(visible).click();
	}
}
