package io.syndesis.qe.fragments.common.menu;

import static com.codeborne.selenide.Condition.visible;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KebabMenu {

    private SelenideElement menuButton;

    public KebabMenu(SelenideElement menuButton) {
        this.menuButton = menuButton;
    }

    public void open() {
        if(menuButton.$(By.xpath("/ul")).is(visible)) {
            return;
        } else {
            menuButton.shouldBe(visible).click();
        }
    }

    public void close() {
        if(menuButton.$(By.xpath("/ul")).is(visible)) {
            menuButton.shouldBe(visible).click();
        }
    }

    public SelenideElement getItemElement(String item) {
        return menuButton.$(By.xpath("//a[text()='" + item + "']")).shouldBe(visible);
    }

    public void checkActionsSet(String[] expectedItems) {
        Assertions.assertThat(getItemsStrings().containsAll(Arrays.asList(expectedItems)));
    }

    private List<String> getItemsStrings() {
        List<String> strings = new ArrayList<String>();
        for(SelenideElement item: menuButton.$$(By.xpath("//a"))) {
            strings.add(item.getText());
        }

        return strings;
    }
}
