package io.syndesis.qe.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.codeborne.selenide.Condition;

public class Conditions {

    public static final Condition NO_CHILDREN = new Condition("No children") {
        @Override
        public boolean apply(WebElement element) {
            return element.findElements(By.cssSelector("*")).isEmpty();
        }
    };
}
