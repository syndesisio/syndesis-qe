package io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FieldFiller extends ConfigureAction {

    public abstract Class getInputClass();

    public abstract Class getSelectClass();

    public SelenideElement checkAndGetFieldTypeById(String elementId) {
        log.info("field-id: {} is being checked", elementId);
        By elem = By.id(elementId);
        return this.checkAndGet(elem);
    }

    public SelenideElement checkAndGetFieldTypeByName(String elementName) {
        log.info("field-name: {} is being checked", elementName);
        By elem = By.name(elementName);
        return this.checkAndGet(elem);
    }

    private SelenideElement checkAndGet(By elem) {

        Class inputClass = getInputClass();
        Class selectClass = getSelectClass();

        SelenideElement element = this.getRootElement().find(elem).shouldBe(visible);
        String elemTagName = element.getTagName();
        if (("input".equals(elemTagName) || "textarea".equals(elemTagName)) && isContainedInLocators(elem, inputClass) ||
                "select".equals(elemTagName) && isContainedInLocators(elem, selectClass)) {
            return element;
        } else {
            return null;
        }

    }

    private boolean isContainedInLocators(By by, Class c) {
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
