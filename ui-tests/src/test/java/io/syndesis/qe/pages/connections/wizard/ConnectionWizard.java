package io.syndesis.qe.pages.connections.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.springframework.stereotype.Component;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.WizardPageObject;
import io.syndesis.qe.pages.connections.wizard.phases.NameConnection;
import io.syndesis.qe.pages.connections.wizard.phases.SelectConnectionType;
import io.syndesis.qe.logic.common.wizard.WizardPhase;

@Component
public class ConnectionWizard extends WizardPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-connection-create-page");
    }

    public ConnectionWizard() {
        setSteps(new WizardPhase[] {new SelectConnectionType(), null, new NameConnection()});
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }
}
