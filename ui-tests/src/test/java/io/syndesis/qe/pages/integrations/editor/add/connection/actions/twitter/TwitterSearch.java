package io.syndesis.qe.pages.integrations.editor.add.connection.actions.twitter;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitterSearch extends ConfigureAction {

    public void fillInput(String value) {
        log.debug("setting keywords element of twitter search with value: {}", value);
        HashMap<By, String> fillMap = new HashMap<By, String>();
        fillMap.put(Input.KEYWORLDS, value);
        this.fillForm(fillMap, this.getRootElement());
    }

    private static final class Input {
        public static final By KEYWORLDS = By.id("keywords");
    }

    public SelenideElement keywordsElement() {
        return this.getRootElement().find(Input.KEYWORLDS).shouldBe(visible);
    }

    public void fillKeywordsValue(String value) {

    }
}
