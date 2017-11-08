package io.syndesis.qe.pages.connections;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.Map;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/12/17.
 */
@Slf4j
public class ConnectionConfigurationComponent extends SyndesisPageObject {

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	private static final class Element {

		public static final By ROOT = By.cssSelector("syndesis-connections-configure-fields");
	}

	/**
	 * Fill connection details from given connection to ui
	 *
	 * @param conneDetails <key> : <value> object with data
	 * @returns {any} resolved promise once all filled
	 */
	public void fillDetails(Map<String, String> conneDetails) throws Exception {

		if (conneDetails.isEmpty()) {
			throw new Exception(String.format("can't find any connection details in %s", "d"));
		}
		for (String key : conneDetails.keySet()) {
			log.info(String.format("fill conneDetails detail %s => %s", key, conneDetails.get(key)));
			getRootElement().$(String.format("input[name=\"%s\"")).shouldBe(visible).sendKeys(conneDetails.get(key));
		}
	}
}
