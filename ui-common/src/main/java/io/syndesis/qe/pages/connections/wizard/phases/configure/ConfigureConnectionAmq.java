package io.syndesis.qe.pages.connections.wizard.phases.configure;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

public class ConfigureConnectionAmq extends ConfigureConnection {

    private static final class Label {
        public static final By PAGE_TITLE = By.cssSelector("h2[innertext='ActiveMQ Configuration']");
    }

    @Override
    public boolean validate() {
        return this.getRootElement().find(Label.PAGE_TITLE).is(visible);
    }
}
