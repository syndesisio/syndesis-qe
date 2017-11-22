package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationEditPage extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-edit-page");

		public static final By TRASH = By.className("fa-trash");
		public static final By DELETE = By.className("delete-icon");
	}

	@Override
	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	@Override
	public boolean validate() {
		return getRootElement().is(visible);
	}

	public ActionConfigureComponent actionConfigureComponent() {
		return new ActionConfigureComponent();
	}

	public FlowViewComponent flowViewComponent() {
		return new FlowViewComponent();
	}

	public ConnectionSelectComponent connectionSelectComponent() {
		return new ConnectionSelectComponent();
	}

	public IntegrationBasicsComponent basicsComponent() {
		return new IntegrationBasicsComponent();
	}

	public ElementsCollection getAllTrashes() {
		return this.getRootElement().findAll(Element.TRASH);
	}
	public ElementsCollection getAllDeletes() {
		return this.getRootElement().findAll(Element.DELETE);
	}
	public void clickRandomTrash() {
		this.clickElementRandom(Element.TRASH);
	}

}
