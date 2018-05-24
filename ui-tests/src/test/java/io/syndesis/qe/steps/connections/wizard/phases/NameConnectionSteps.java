package io.syndesis.qe.steps.connections.wizard.phases;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.connections.wizard.phases.NameConnection;
import io.syndesis.qe.steps.CommonSteps;

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


    @When("^fills? Name Connection form$")
    public void fillNameConnectionForm(DataTable data) {
        new Form(nameConnectionPage.getRootElement()).fillByLabel(data.asMap(String.class, String.class));
    }
}
