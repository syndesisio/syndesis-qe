package io.syndesis.qe.pages.integrations.edit;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

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
	}

	private static final class CssClasses {
		public static final String TRASH_CLASS = "fa-trash";
		public static final String DELETE_CLASS = "delete-icon";
	}

	public void checkPageIsPresent(String alias) {
		String elementName = "";
		switch (alias) {
			case "Choose a Finish Connection":
			case "Add to Integration": {
				elementName = "h1";
				break;
			}
			default: {
				log.info("Page {} text doesn't exist", alias);
				assertThat(true, is(false));
			}
		}
		this.getRootElement().shouldBe(visible).find(By.cssSelector(String.format("%s[innertext='%s']", elementName, alias))).shouldBe(visible);
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
		return getElementsByClassName(CssClasses.TRASH_CLASS);
	}
	public ElementsCollection getAllDeletes() {
		return getElementsByClassName(CssClasses.DELETE_CLASS);
	}
	public void clickRandomTrash() {
		clickElementRandom(CssClasses.TRASH_CLASS);
	}

}
