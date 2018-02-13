package io.syndesis.qe.steps.connections.wizard.phases;

import cucumber.api.java.en.When;
import io.syndesis.qe.pages.connections.wizard.phases.NameConnection;

public class NameConnectionSteps {

    private final NameConnection nameConnectionPage = new NameConnection();

    @When("^types? \"([^\"]*)\" into connection name$")
    public void setConnectionName(String name) {
        nameConnectionPage.setName(name);
    }

    @When("^types? \"([^\"]*)\" into connection description$")
    public void setConnectionDescription(String description) {
        nameConnectionPage.setDescription(description);
    }

}
