package io.syndesis.qe.pages.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.list.CardList;
import io.syndesis.qe.fragments.common.list.actions.ListAction;
import io.syndesis.qe.pages.connections.fragments.list.ConnectionsList;
import io.syndesis.qe.pages.connections.wizard.ConnectionWizard;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnection;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnectionAmq;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@Component
public class SelectConnectionType extends AbstractConnectionWizardStep {

    @Autowired
    private ConnectionWizard wizard;

    private static final class Element {
        public static final By ROOT = By.cssSelector(".pf-c-page__main-section.pf-m-no-padding");
    }

    public static final By ROOT = By.cssSelector(".pf-c-page__main-section.pf-m-no-padding");
    private CardList connectionTypes = new ConnectionsList(By.cssSelector(".pf-c-page__main-section"));


    public void selectConnectionType(String title) {
        switch (title) {
            case "Red Hat AMQ":
                wizard.replaceStep(new ConfigureConnectionAmq(), 1);
                break;
            default:
                wizard.replaceStep(new ConfigureConnection(), 1);
        }

        log.info("Selected " + title + " connection type.");
        connectionTypes.invokeActionOnItem(title, ListAction.CLICK);
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }
}
