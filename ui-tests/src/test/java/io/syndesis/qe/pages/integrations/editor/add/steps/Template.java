package io.syndesis.qe.pages.integrations.editor.add.steps;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.AbstractStep;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.util.Sets;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Template extends AbstractStep {
    public enum WarningType {
        WARNING,
        ERROR
    }

    private static final class CodeEditor {
        public static final By CODE_EDITOR_LINES = By.cssSelector(".CodeMirror-code > div,pre");
        public static final By CODE_EDITOR_WARNINGS = By.cssSelector(".CodeMirror-gutter-elt > *");
        public static final By CODE_EDITOR_TOOLTIP = By.cssSelector(".CodeMirror-lint-tooltip");

        //CSS classes for checking elements in code editor
        public static final String ERROR_MESSAGE_CLASS = "CodeMirror-lint-message-error";
        public static final Set<String> WARNINGS = Sets.newTreeSet("CodeMirror-lint-marker-error", "CodeMirror-lint-marker-warning");
        public static final Map<String, By> CODE_HIGHLIGHT_MAP = new ImmutableMap.Builder<String, By>()
            .put("Freemarker", By.className("cm-freemarker"))
            .put("Mustache", By.className("cm-mustache"))
            .put("Velocity", By.className("cm-builtin"))
            .build();
    }

    private static final class Elements {
        public static final By TEMPLATE = By.cssSelector(".CodeMirror textarea");
        public static final By FILE_INPUT = By.className("templater-form-file-chooser");
        public static final By TEMPLATE_LANGUAGES = By.className("template-language-choices");
        public static final By CHECKED_TEMPLATE_LANGUAGE = By.cssSelector("input[type='radio']:checked + label");

        public static final String UPLOAD_CLASS = "file-upload-input";
    }

    @Data
    public static class CodeEditorWarning {
        private final WarningType type;
        private final List<String> messages;

        private static CodeEditorWarning getWarningMessage() {
            List<String> messages = $(CodeEditor.CODE_EDITOR_TOOLTIP)
                .findAll("*")
                .stream()
                .map(SelenideElement::innerText)
                .collect(Collectors.toList());

            boolean isError = $(CodeEditor.CODE_EDITOR_TOOLTIP)
                .findAll("*")
                .stream()
                .anyMatch(selenideElement -> {
                    log.debug("Lint marker: {}", selenideElement);
                    log.debug(selenideElement.attr("class"));
                    return CodeEditor.ERROR_MESSAGE_CLASS.equals(selenideElement.attr("class"));
                });
            return new CodeEditorWarning(isError ? WarningType.ERROR : WarningType.WARNING, messages);
        }
    }

    @Setter
    @Getter
    private String templateDef;

    public Template(String templateDefinition) {
        super();
        setTemplateDef(templateDefinition);
    }

    @Override
    public void fillConfiguration() {
        String template = getTemplateDef();
        setTemplate(template);
    }

    @Override
    public boolean validate() {
        return getTextAreaElement().shouldBe(visible).isDisplayed();
    }

    @Override
    public void initialize() {
        String template = getTemplateTextAreaDef();
        setTemplateDef(template);
    }

    @Override
    public String getParameter() {
        return getTemplateDef();
    }

    @Override
    public void setParameter(String parameter) {
        setTemplateDef(parameter);
    }

    public String getTemplateTextAreaDef() {
        return getTextAreaElement().shouldBe(visible).getValue();
    }

    public void setTemplateTextAreaDef(String areaDef) {
        getTextAreaElement().sendKeys(areaDef);
    }

    private SelenideElement getTextAreaElement() {
        SelenideElement textarea = this.getRootElement().find(Elements.TEMPLATE);
        if ("none".equals(textarea.attr("display"))) {
            JavascriptExecutor jse = (JavascriptExecutor) WebDriverRunner.getWebDriver();
            jse.executeScript("arg[0].style.display='';", textarea);
            WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), 20);
            wait.until(ExpectedConditions.visibilityOf(textarea));
        }
        return textarea;
    }

    public List<String> getHighlightedWords() {
        //Selects the label of selected option radio button
        SelenideElement selectedType = getRootElement().find(Elements.TEMPLATE_LANGUAGES).find(Elements.CHECKED_TEMPLATE_LANGUAGE);
        By highlightSelector = CodeEditor.CODE_HIGHLIGHT_MAP.get(selectedType.innerText());
        return getRootElement().findAll(CodeEditor.CODE_EDITOR_LINES).stream()
            .map(line -> line.findAll(highlightSelector)).flatMap(Collection::stream)
            .map(SelenideElement::innerText).collect(Collectors.toList());
    }

    public void setTemplate(String template) {
        if (WebDriverRunner.isFirefox()) {
            log.info("template text area: {}", getTextAreaElement());
            getTextAreaElement().sendKeys(template);
            //Should validate the textarea to enable the next button if the template is valid
            getRootElement().find(By.tagName("form")).click();
        } else {
            SelenideElement cm = getRootElement().find(By.className("CodeMirror"));
            WebDriver driver = WebDriverRunner.getWebDriver();
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", cm, template);
            WebDriverWait wait = new WebDriverWait(driver, 20);
            wait.until(ExpectedConditions.textToBe(By.cssSelector(".CodeMirror"), template));
        }
        flipThroughOptions();
    }

    public void setTemplatingEngine(String engine) {
        getRootElement().find(By.id(engine.toLowerCase() + "-choice")).shouldBe(visible).click();
    }

    public void uploadTemplate(String filePath) {
        File tplFile = new File(filePath);
        assertThat(tplFile).exists().canRead();
        //Make file input element visible
        SelenideElement upload = getRootElement().find(Elements.FILE_INPUT);
        WebDriver driver = WebDriverRunner.getWebDriver();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("arguments[0].className=arguments[1];", upload.parent(), Elements.UPLOAD_CLASS);
        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.elementToBeClickable(upload));
        log.info("uploading file " + tplFile.getAbsolutePath());
        upload.uploadFile(tplFile);
        log.info("done uploading");
        flipThroughOptions();
    }

    public Optional<CodeEditorWarning> getWarningsOnLine(int lineNum) {
        // The entire div containing the line and any possible error markers
        SelenideElement line = getRootElement().$$(CodeEditor.CODE_EDITOR_LINES).get(lineNum - 1);
        if (line.find("div").exists()) {
            //Line contains marker indicating warnings
            hoverOnErrorMarker(line.find(CodeEditor.CODE_EDITOR_WARNINGS));
            return Optional.of(CodeEditorWarning.getWarningMessage());
        }

        return Optional.empty();
    }

    public List<CodeEditorWarning> getAllWarnings() {
        /*
            In .CodeMirror-code every line is represented by one div
            If the line doesn't have any errors there is just pre.CodeMirror-line inside the div
            But if there is any error there are also divs for displaying the error marker
         */
        return getRootElement().$$(CodeEditor.CODE_EDITOR_LINES).stream()
            .map(element -> element.$$("div"))
            .filter(((Predicate<ElementsCollection>) (ElementsCollection::isEmpty)).negate())
            .flatMap(ElementsCollection::stream)
            .filter(el -> CodeEditor.WARNINGS.contains(el.attr("class")))
            .map(el -> {
                hoverOnErrorMarker(el);
                return CodeEditorWarning.getWarningMessage();
            }).collect(Collectors.toList());
    }

    private void hoverOnErrorMarker(SelenideElement marker) {
        //Remove from the original line element
        getRootElement().hover();
        //There can be more than 1 editor warning in the DOM despite only one being visible
        TestUtils.waitFor(
            () -> $$(CodeEditor.CODE_EDITOR_TOOLTIP).size() == 0,
            1, 10, "There is more code editor tooltips at one time");
        marker.hover();
    }

    private void flipThroughOptions() {
        //The template in textarea validates when the template type is changed
        log.info("Clicking through all template options to validate the template");
        SelenideElement radioGroup = getRootElement().find(Elements.TEMPLATE_LANGUAGES);
        ElementsCollection options = radioGroup.findAll(By.cssSelector("input[type='radio']"));
        String validId = options.filterBy(checked).first().attr("id");
        options.forEach(SelenideElement::click);
        radioGroup.find(By.id(validId)).click();
        log.info("Template should be validated");
    }
}
