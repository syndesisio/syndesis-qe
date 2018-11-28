package io.syndesis.qe.pages.integrations.editor.apiprovider;

import static com.codeborne.selenide.Condition.visible;
import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;

public class ChooseOperation extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-api-provider-operations");
    }
    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }
}
