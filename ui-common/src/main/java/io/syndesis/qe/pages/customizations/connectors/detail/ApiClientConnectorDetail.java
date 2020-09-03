package io.syndesis.qe.pages.customizations.connectors.detail;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ApiClientConnectorDetail extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = ByUtils.dataTestId("api-connector-detail-body");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }
}
