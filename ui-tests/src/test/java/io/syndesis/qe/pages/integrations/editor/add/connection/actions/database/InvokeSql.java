package io.syndesis.qe.pages.integrations.editor.add.connection.actions.database;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 12/7/17.
 */
@Slf4j
public class InvokeSql extends Sql {
    private static final class Element {
        public static final By INPUT_QUERY = By.cssSelector("input[name='query']");
        public static final By TITLE = By.cssSelector("h3[innertext='Invoke SQL']");
    }

    @Override
    public void fillSqlInput(String query) {
        log.debug("filling sql query: {}", query);
        SelenideElement element = $(Element.INPUT_QUERY);
        this.fillInput(element, query);
    }

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

}
