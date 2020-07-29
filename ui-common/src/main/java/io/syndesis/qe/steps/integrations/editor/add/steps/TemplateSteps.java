package io.syndesis.qe.steps.integrations.editor.add.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.integrations.editor.add.steps.Template;
import io.syndesis.qe.pages.integrations.editor.add.steps.Template.CodeEditorWarning;
import io.syndesis.qe.pages.integrations.editor.add.steps.Template.WarningType;

import java.util.List;
import java.util.Optional;

import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemplateSteps {

    Template template = new Template("");

    @Then("^set the template to \"([^\"]*)\"$")
    public void setTemplate(String tmpl) {
        template.setTemplate(tmpl);
    }

    @Then("^set the template type to \"([^\"]*)\"$")
    public void setTemplateType(String type) {
        template.setTemplatingEngine(type);
    }

    @Then("^upload template from file \"([^\"]*)\"$")
    public void uploadFile(String path) {
        template.uploadTemplate(path);
    }

    @Then("^upload template from resource \"([^\"]*)\"$")
    public void uploadTemplateFromResource(String file) {
        uploadFile("src/test/resources/" + file);
    }

    @Then("^check that number of warnings in the template editor is (\\d+)$")
    public void checkWarningCount(int count) {
        assertThat(template.getAllWarnings()).hasSize(count);
    }

    @Then("^check that there is no warning on line (\\d+) in the template editor$")
    public void checkLineWithoutError(int lineNum) {
        assertThat(template.getWarningsOnLine(lineNum)).isNotPresent();
    }

    @Then("^check that there is (error|warning) on line (\\d+) in the template editor$")
    public void checkLineError(String type, int lineNum) {
        Optional<CodeEditorWarning> warning = template.getWarningsOnLine(lineNum);
        assertThat(warning).isPresent();
        assertThat(warning.get().getType()).isEqualTo(WarningType.valueOf(type.toUpperCase()));
    }

    @Then("^check that there is an error message with text \"([^\"]*)\" in the template editor$")
    public void checkErrorMessage(String msg) {
        List<CodeEditorWarning> warnings = template.getAllWarnings();
        assertThat(warnings.stream().anyMatch(codeEditorWarning -> codeEditorWarning.getMessages().contains(msg))).isTrue();
    }

    @Then("^check that \"([^\"]*)\" is highlighted in the template editor$")
    public void wordShouldBeHighlighted(String word) {
        assertThat(template.getHighlightedWords()).contains(word);
    }
}
