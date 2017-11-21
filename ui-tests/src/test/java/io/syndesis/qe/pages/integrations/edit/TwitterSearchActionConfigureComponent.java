package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;

import java.util.HashMap;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitterSearchActionConfigureComponent extends ActionConfigureComponent {

	private static final class Input {
		public static final By KEYWORLDS = By.id("keywords");
	}

	public SelenideElement keywordsElement() {
		return this.getRootElement().find(Input.KEYWORLDS).shouldBe(visible);
	}

	public void fillKeywordsValue(String value) {
		log.debug("setting keywords element of twitter search with value: {}", value);
		HashMap<By, String> fillMap = new HashMap<By, String>();
		fillMap.put(Input.KEYWORLDS, value);
		this.fillForm(fillMap, this.getRootElement());
	}
}
