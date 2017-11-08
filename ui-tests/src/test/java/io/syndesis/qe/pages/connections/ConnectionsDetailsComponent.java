package io.syndesis.qe.pages.connections;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/12/17.
 */
@Slf4j
public class ConnectionsDetailsComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-connections-review");
		public static final By DESCRIPTION = By.cssSelector("textarea[data-id=\"descriptionInput\"]");
		public static final By INPUT_NAME = By.cssSelector("input[data-id=\"nameInput\"]");
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	public SelenideElement getDescription() {
		final SelenideElement description = $(Element.ROOT).find(Element.DESCRIPTION);
		return description;
	}

	public SelenideElement getInputName() {
		final SelenideElement inputName = $(Element.ROOT).find(Element.INPUT_NAME);
		return inputName;
	}
}
