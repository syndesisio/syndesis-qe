package io.syndesis.qe.pages.integrations.editor.add;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

public class ChooseFinishConnection extends ChooseConnection {

    private static final class Element {
        public static final By TITLE = By.cssSelector("h1[innertext='Choose a Finish Connection']");
    }

    @Override
    public boolean validate() {
        return this.getRootElement().shouldBe(visible).find(Element.TITLE).is(visible);
    }
}
