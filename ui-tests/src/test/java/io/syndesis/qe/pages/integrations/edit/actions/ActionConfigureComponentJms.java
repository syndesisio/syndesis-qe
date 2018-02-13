package io.syndesis.qe.pages.integrations.edit.actions;

import static com.codeborne.selenide.Condition.visible;

import java.lang.reflect.Field;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ActionConfigureComponentJms extends ActionConfigureComponent {

    abstract Class getInputClass();

    abstract Class getSelectClass();

    public SelenideElement checkAndGetFieldType(String elementId) {
        log.info("field: {} is being checked", elementId);
        By elem = By.id(elementId);

        Class inputClass = getInputClass();
        Class selectClass = getSelectClass();

        SelenideElement element = this.getRootElement().find(elem).shouldBe(visible);
        if (element.getTagName().equals("input") && isContainedInLocators(elem, inputClass) ||
                element.getTagName().equals("select") && isContainedInLocators(elem, selectClass)) {
            return element;
        } else {
            return null;
        }
    }

    public boolean isContainedInLocators(By by, Class c) {
        Field[] fields = c.getDeclaredFields();

        for (Field field : fields) {
            try {
                if (by.equals(field.get(null))) {
                    log.info("CONTAINED " + field.getName() + " - " + field.getType());
                    return true;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.warn("Error in {} class: {}", this.getClass().getCanonicalName(), e.toString());
                return false;
            }
        }
        return false;
    }
}
