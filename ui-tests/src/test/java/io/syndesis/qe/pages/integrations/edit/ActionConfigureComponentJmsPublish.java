package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 12/19/17.
 */
@Slf4j
public class ActionConfigureComponentJmsPublish extends ActionConfigureComponentJms {

	private static final class Element {
		public static final By TITLE = By.cssSelector("div[innertext='Publish Messages']");
	}

	private static final class Input {
		public static final By DESTINATION_NAME = By.id("destinationName");
		public static final By PERSISTENT = By.id("persistent");
	}

	private static final class Select {
		public static final By DESTINATION_TYPE = By.id("destinationType");
	}


	@Override
	public boolean validate() {
		return this.getRootElement().find(Element.TITLE).is(visible);
	}

	@Override
	public Class getInputClass() {
		return ActionConfigureComponentJmsPublish.Input.class;
	}

	@Override
	public Class getSelectClass() {
		return ActionConfigureComponentJmsPublish.Select.class;
	}
}
