package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class Home extends MainPage {

    public void checkVisibility(boolean shouldBeVisible) {
        if (shouldBeVisible) {
            $(Tab.HOME).shouldBe(visible);
        } else {
            $(Tab.HOME).shouldNotBe(visible);
        }
    }
}
