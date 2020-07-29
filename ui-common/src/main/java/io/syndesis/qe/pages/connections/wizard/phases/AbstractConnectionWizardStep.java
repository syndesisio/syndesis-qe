package io.syndesis.qe.pages.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.logic.common.wizard.WizardPhase;

public abstract class AbstractConnectionWizardStep  extends SyndesisPageObject implements WizardPhase {
    @Override
    public void goToNextWizardPhase() {
        new SyndesisRootPage().getButton("Next").shouldBe(visible).click();
    }
}
