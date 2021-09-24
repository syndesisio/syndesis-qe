package io.syndesis.qe.utils;

import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

public class UIUtils {

    private static class Elements {
        public static By LOADER = By.cssSelector(".Loader");
        public static By DATAMAPPER_LOADER = ByUtils.dataTestId("enable-disable-conditional-mapping-expression-button");
    }

    public static void ensureUILoaded() {
        TestUtils.waitFor(() -> !$(Elements.LOADER).exists(), 1, 10, "Syndesis UI is still loading");
    }

    public static void ensureDataMapperUILoaded() {
        TestUtils.waitFor(() -> $(Elements.DATAMAPPER_LOADER).is(Condition.visible), 1, 10, "DataMapper UI is still loading");
    }
}
