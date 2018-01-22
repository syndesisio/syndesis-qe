package io.syndesis.qe.pages.dialogs;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class DeleteWarningDialog extends SyndesisPageObject {

	private static class Element {
		public static By ROOT = By.xpath("//div[@class='modal-content']");
	}
	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		try {
			getRootElement();
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
}
