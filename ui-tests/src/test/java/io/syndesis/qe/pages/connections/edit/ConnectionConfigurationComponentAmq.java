package io.syndesis.qe.pages.connections.edit;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

/**
 * Created by sveres on 12/18/17.
 */
public class ConnectionConfigurationComponentAmq extends ConnectionConfigurationComponent {

	private static final class Element {
		public static final By TITLE = By.cssSelector("h2[innertext='ActiveMQ Configuration']");
	}

	@Override
	public boolean validate() {
		return this.getRootElement().find(Element.TITLE).is(visible);
	}
}
