package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syndesis.qe.pages.MultipleInputs;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 12/19/17.
 */
@Slf4j
public class ActionConfigureComponentJmsSubscribe extends ActionConfigureComponent implements MultipleInputs {


	private static final class Element {
		public static final By TITLE = By.cssSelector("div[innertext='Subscribe for Messages']");
	}

	private static final class Input {
		public static final By DESTINATION_NAME = By.id("destinationName");
		public static final By SUBCRIPTION_ID = By.id("durableSubscriptionId");
		public static final By MESSAGE_SELECTOR = By.id("messageSelector");
		public static final List<By> ALL_INPUTS = new ArrayList<By>(Arrays.asList(DESTINATION_NAME,SUBCRIPTION_ID,MESSAGE_SELECTOR));
	}

	private static final class Select {
		public static final By DESTINATION_TYPE = By.id("destinationType");
		public static final List<By> ALL_SELECTS = new ArrayList<By>(Arrays.asList(DESTINATION_TYPE));
	}

	@Override
	public boolean validate() {
		return this.getRootElement().find(Element.TITLE).is(visible);
	}

	@Override
	public String checkAndGetFieldType(String field) {
		log.info("field: {} is being checked", field);
		By elem = By.id(field);
		String tagName = this.getRootElement().$(elem).getTagName();
		if ("input".equals(tagName) && Input.ALL_INPUTS.contains(elem) || "select".equals(tagName) && Select.ALL_SELECTS.contains(elem)) {
			return tagName;
		} else {
			return null;
		}
	}

}
