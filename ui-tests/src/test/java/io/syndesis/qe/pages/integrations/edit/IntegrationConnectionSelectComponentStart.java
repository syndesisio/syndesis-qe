package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

public class IntegrationConnectionSelectComponentStart extends IntegrationConnectionSelectComponent {
	
	private static final class Element {
		public static final By TITLE = By.cssSelector("h1[innertext='Choose a Start Connection']");
	}

	public boolean validate() {
		return this.getRootElement().shouldBe(visible).find(Element.TITLE).is(visible);
	}
}
