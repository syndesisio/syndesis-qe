package io.syndesis.qe.fragments.common.menu;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static org.assertj.core.api.Assertions.assertThat;

public class KebabMenu {

    private SelenideElement menuButton;

    public KebabMenu(SelenideElement menuButton) {
        this.menuButton = menuButton;
    }

    public void open() {
        if (menuButton.$(By.xpath("/ul")).is(visible)) {
            return;
        } else {
            menuButton.shouldBe(visible).click();
        }
    }

    public void close() {
        if (menuButton.$(By.xpath("/ul")).is(visible)) {
            menuButton.shouldBe(visible).click();
        }
    }

    public SelenideElement getItemElement(String item) {
        ElementsCollection found = menuButton.parent().$$(By.className("dropdown-item")).filter(Condition.exactText(item));
        assertThat(found)
                .size().isEqualTo(1);
        return found.first();
    }

    public void checkActionsSet(String[] expectedItems) {
        assertThat(getItemsStrings().containsAll(Arrays.asList(expectedItems)));
    }

    private List<String> getItemsStrings() {
        List<String> strings = new ArrayList<String>();
        for (SelenideElement item : menuButton.$$(By.xpath("//a"))) {
            strings.add(item.getText());
        }

        return strings;
    }
}
