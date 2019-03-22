package io.syndesis.qe.steps.integrations.editor.add.steps;

import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.integrations.editor.add.steps.Template;

public class TemplateSteps {

    Template page = new Template("");

    @Then("^inputs? template \"([^\"]*)\"$")
    public void setTemplate(String template) {
        page.setTemplate(template);
    }

    @Then("^sets? the template type to \"([^\"]*)\"$")
    public void setEngine(String engine) {
        page.setTemplatingEngine(engine);
    }

    @Then("^uploads? template from file \"([^\"]*)\"$")
    public void uploadTemplate(String file) {
        page.uploadTemplate(file);
    }

    @Then("^uploads? template from resource \"([^\"]*)\"$")
    public void uploadTemplateFromResource(String file) {
        uploadTemplate("src/test/resources/" + file);
    }

}
