package io.syndesis.qe.pages.connections.wizard.phases.configure;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.connections.wizard.phases.AbstractConnectionWizardStep;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.Map;

import lombok.Getter;

@Getter
public class ConfigureConnection extends AbstractConnectionWizardStep {

    private static final class Element {
        public static final By ROOT = By.cssSelector("pf-c-page__main-wizard");
    }

    private Form form = new Form($(Element.ROOT));

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }

    public void fillForm(Map<String, String> data) {
        form.fillByName(data);
    }
}
