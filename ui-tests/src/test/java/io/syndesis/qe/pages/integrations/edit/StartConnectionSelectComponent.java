package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

public class StartConnectionSelectComponent extends ConnectionSelectComponent {
	
	private static final class Element {
		public static final By TITLE = By.cssSelector("h1[innertext='Add to Integration']");
	}

	public boolean validate() {
		return this.getRootElement().shouldBe(visible).find(Element.TITLE).is(visible);
	}
}
