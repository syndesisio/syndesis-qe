package io.syndesis.qe.pages.integrations.editor.add.steps;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.integrations.editor.add.steps.getridof.AbstractStep;
import io.syndesis.qe.utils.Conditions;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        public static final By CODE_EDITOR_LINES = By.cssSelector(".CodeMirror-code > div");
        public static final By CODE_EDITOR_WARNINGS = By.className("CodeMirror-lint-marker-warning");
        public static final By CODE_EDITOR_ERRORS = By.className("CodeMirror-lint-marker-error");
        public static final By CODE_EDITOR_TOOLTIP = By.cssSelector(".CodeMirror-lint-tooltip");
        public static final By LINE_GUTTER = By.className("CodeMirror-gutter-wrapper");

        //CSS classes for checking elements in code editor
        public static final String ERROR_MESSAGE_CLASS = "CodeMirror-lint-message-error";
        public static final Map<String, By> CODE_HIGHLIGHT_MAP = new ImmutableMap.Builder<String, By>()
            .put("Freemarker", By.className("cm-freemarker"))
            .put("Mustache", By.className("cm-mustache"))
            .put("Velocity", By.className("cm-builtin"))
            .build();
    }

    private static final class Elements {
        public static final By TEMPLATE = By.cssSelector(".CodeMirror textarea");
        public static final By FILE_INPUT = By.cssSelector(".dropzone input");
        public static final By TEMPLATE_LANGUAGES = By.className("template-language-choices");
        public static final By CHECKED_TEMPLATE_LANGUAGE = By.cssSelector("input[type='radio']:checked");
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
                    return Objects.requireNonNull(selenideElement.attr("class")).contains(CodeEditor.ERROR_MESSAGE_CLASS);
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
            WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOf(textarea));
        }
        return textarea;
    }

    public List<String> getHighlightedWords() {
        //Selects the label of selected option radio button
        SelenideElement selectedType = getRootElement().find(Elements.TEMPLATE_LANGUAGES).find(Elements.CHECKED_TEMPLATE_LANGUAGE).parent();
        By highlightSelector = CodeEditor.CODE_HIGHLIGHT_MAP.get(selectedType.innerText());
        return getRootElement().findAll(CodeEditor.CODE_EDITOR_LINES).exclude(Conditions.STALE_ELEMENT).stream()
            .map(line -> line.findAll(highlightSelector)).flatMap(Collection::stream)
            .map(SelenideElement::innerText).collect(Collectors.toList());
    }

    public void setTemplate(String template) {
        if (TestConfiguration.syndesisBrowser().contains("firefox")) {
            log.info("template text area: {}", getTextAreaElement());
            getTextAreaElement().sendKeys(template);
            //Should validate the textarea to enable the next button if the template is valid
            getRootElement().find(By.tagName("form")).click();
        } else {
            SelenideElement cm = getRootElement().find(By.className("CodeMirror"));
            WebDriver driver = WebDriverRunner.getWebDriver();
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", cm, template);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".CodeMirror"), template));
        }
    }

    public void setTemplatingEngine(String engine) {
        getRootElement().find(By.id(engine.toLowerCase() + "-choice")).shouldBe(visible).click();
    }

    public void uploadTemplate(String filePath) {
        File tplFile = new File(filePath);
        assertThat(tplFile).exists().canRead();
        //Make file input element visible
        SelenideElement upload = $(Elements.FILE_INPUT);
        WebDriver driver = WebDriverRunner.getWebDriver();
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("arguments[0].style.display=\"block\";", upload);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(upload));
        log.info("uploading file " + tplFile.getAbsolutePath());
        upload.uploadFile(tplFile);
        log.info("done uploading");
    }

    public Optional<CodeEditorWarning> getWarningsOnLine(int lineNum) {
        // The entire div containing the line and any possible error markers
        SelenideElement line = getRootElement().$$(CodeEditor.CODE_EDITOR_LINES).get(lineNum - 1);
        if (line.find(CodeEditor.LINE_GUTTER).findAll("*").size() > 1) {
            //Line contains marker indicating warnings
            SelenideElement warning = line.find(CodeEditor.CODE_EDITOR_WARNINGS);
            if (!warning.exists()) {
                warning = line.find(CodeEditor.CODE_EDITOR_ERRORS);
            }
            hoverOnErrorMarker(warning);
            return Optional.of(CodeEditorWarning.getWarningMessage());
        }

        return Optional.empty();
    }

    public List<CodeEditorWarning> getAllWarnings() {
        List<CodeEditorWarning> result = new ArrayList<>();
        int size = getRootElement().$$(CodeEditor.CODE_EDITOR_LINES).exclude(Conditions.STALE_ELEMENT).size();
        for (int i = 1; i <= size; i++) {
            Optional<CodeEditorWarning> warningsOnLine = getWarningsOnLine(i);
            warningsOnLine.ifPresent(result::add);
        }
        return result;
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
}
