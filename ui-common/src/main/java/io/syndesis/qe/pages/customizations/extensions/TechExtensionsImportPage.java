package io.syndesis.qe.pages.customizations.extensions;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class TechExtensionsImportPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-tech-extentions-import");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }
}
