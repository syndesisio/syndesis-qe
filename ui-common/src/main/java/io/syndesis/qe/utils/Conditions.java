package io.syndesis.qe.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;

public class Conditions {

    public static final Condition HAS_CHILDREN = new Condition("Has children") {
        @Override
        public boolean apply(Driver driver, WebElement webElement) {
            return !(webElement.findElements(By.cssSelector("*")).isEmpty());
        }
    };
    public static final Condition WO_CLOSE_BUTTONS = new Condition("With out close buttons") {
        @Override
        public boolean apply(Driver driver, WebElement webElement) {
            return webElement.findElements(Alert.Element.CLOSE_BUTTON).isEmpty();
        }
    };
    public static final Condition STALE_ELEMENT = new Condition("Element is stale") {
        @Override
        public boolean apply(Driver driver, WebElement webElement) {
            try {
                webElement.isEnabled();
                return false;
            } catch (StaleElementReferenceException expected) {
                return true;
            }
        }
    };
}
