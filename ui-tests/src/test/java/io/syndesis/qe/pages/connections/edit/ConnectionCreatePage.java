package io.syndesis.qe.pages.connections.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/12/17.
 */
@Getter
@Slf4j
public class ConnectionCreatePage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-connection-create-page");
    }

    private ConnectionConfigurationComponent connectionConfiguration = new ConnectionConfigurationComponent();
    private ConnectionsDetailsComponent connectionDetails = new ConnectionsDetailsComponent();

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }
}
