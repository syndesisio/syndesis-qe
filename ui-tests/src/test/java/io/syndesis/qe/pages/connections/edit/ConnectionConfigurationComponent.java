package io.syndesis.qe.pages.connections.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/12/17.
 */
@Slf4j
public class ConnectionConfigurationComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-connections-configure-fields");
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	/**
	 * Fill connection details from given connection to ui
	 *
	 * @param conneDetails <key> : <value> object with data
	 * @returns {any} resolved promise once all filled
	 */
	public void fillDetails(Map<String, String> conneDetails) {

		if (conneDetails.isEmpty()) {
			throw new IllegalArgumentException("can't find any connection details in connection");
		}

		ElementsCollection inputs = getRootElement().findAll(By.cssSelector("input"));
		List<String> keys = new ArrayList<String>();

		for (SelenideElement input : inputs) {
			String name = input.getAttribute("name");
			keys.add(name);
		}

		for (String key : conneDetails.keySet()) {
			if (keys.contains(key)) {
				log.info("fill conneDetails detail {} ", key);
				SelenideElement input = getRootElement().$(String.format("input[name=\"%s\"", key)).shouldBe(visible);
				input.clear();
				input.sendKeys(conneDetails.get(key));
			} else {
				log.info("Input {} is not present on form!", key);
			}
		}
	}
}
