package io.syndesis.qe.fragments.common.menu;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

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
        return menuButton.$(By.xpath("//a[text()='" + item + "']"));
    }

}
