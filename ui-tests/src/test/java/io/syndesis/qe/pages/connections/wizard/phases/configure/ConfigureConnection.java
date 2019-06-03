package io.syndesis.qe.pages.connections.wizard.phases.configure;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.connections.wizard.phases.AbstractConnectionWizardStep;
import lombok.Getter;
import org.openqa.selenium.By;

import java.util.Map;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;


@Getter
public class ConfigureConnection extends AbstractConnectionWizardStep {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".integration-editor-layout__body");

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
