package io.syndesis.qe.pages.integrations.fragments;

import static org.assertj.core.api.Assertions.fail;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.have;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.list.RowList;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OperationsList extends RowList {
    public OperationsList(By rootElement) {
        super(rootElement);
    }

    private static final class Element {
        public static final By TITLE = By.xpath("//*[contains(@class,'list-pf-title')]");
        public static final By STATUS = By.cssSelector(".list-pf-additional-content div");
        public static final By DESCRIPTION = By.cssSelector(".list-group-item-heading");
        public static final By PATH = By.cssSelector(".item__operation-path");
    }

    public String getStatus(String operationName) {
        return getItem(operationName).$(Element.STATUS).shouldBe(visible).getText().trim();
    }

    public String getVerb(String operationName) {
        return getItem(operationName).$(Element.DESCRIPTION).shouldBe(visible).getText().trim();
    }

    public String getUrl(String operationName) {
        return getItem(operationName).$(Element.PATH).shouldBe(visible).getText().trim();
    }

    @Override
    public SelenideElement getTitle(String title) {
        // operation list does not have the title="..." attribute
        return getRootElement().shouldBe(visible).$$(Element.TITLE).shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1))
            .find(have(exactText(title)));
    }

    public void apiOperationCreateFlow(String title) {
        try {
            OpenShiftWaitUtils.waitFor(() -> $(getItem(title)).is(visible), 15 * 1000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("API provider operation in list was not found in 15s.", e);
        }

        $(getItem(title)).$(ByUtils.dataTestId("api-provider-operations-create-flow")).shouldBe(visible).click();
    }

    public List<String> getOperations() {
        return getRootElement().shouldBe(visible).$$(Element.TITLE).stream()
            .map(SelenideElement::text).collect(Collectors.toList());
    }
}
