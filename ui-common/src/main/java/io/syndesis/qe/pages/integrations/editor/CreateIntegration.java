package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.SelenideElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateIntegration extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("integration-editor-layout__contentOuter");
        public static final By LABELS_ROOT = ByUtils.dataTestId("integration-label-select");
        public static final By LABEL_ELEMENT = By.className("pf-c-chip");
        public static final By DELETE_LABEL_BUTTON = ByUtils.customAttribute("data-ouia-component-id", "Remove");

        public static final By NEW_ENV_BUTTON = ByUtils.dataTestId("form-array-control-add-another-item-button");
        public static final By ENV_ROOT = ByUtils.dataTestId("environment");
        public static final String CONTROLS_PANEL = "environment-%d-array-controls-control";
        public static final By DELETE_ENV_BUTTON = ByUtils.dataTestId("condition-delete");
    }

    private static final class Input {
        public static final By NAME = ByUtils.dataTestId("input", "name");
        public static final By DESCRIPTION = By.cssSelector("textarea[name='description']");
        public static final By NEW_LABEL = By.className("pf-c-select__toggle-typeahead");
        public static final String NEW_ENV_NAME = "environment-%d-key";
        public static final String NEW_ENV_VALUE = "environment-%d-value";
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void setName(String name) {
        log.debug("Setting integration name to {}", name);
        SelenideElement nameElement = this.getRootElement().find(Input.NAME).shouldBe(visible);
        nameElement.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
        nameElement.sendKeys(Keys.BACK_SPACE);
        nameElement.sendKeys(name);
    }

    public void setDescription(String description) {
        log.debug("Setting integration description to {}", description);
        this.getRootElement().find(Input.DESCRIPTION).shouldBe(visible).sendKeys(description);
    }

    public SelenideElement getLabelsRootElement() {
        return getRootElement().find(Element.LABELS_ROOT).should(visible);
    }

    public SelenideElement getEnvsRootElement() {
        return getRootElement().find(Element.ENV_ROOT).should(visible);
    }

    public void addLabel(String label) {
        SelenideElement labelsElement = getLabelsRootElement().find(Input.NEW_LABEL).shouldBe(visible);
        labelsElement.click();
        labelsElement.sendKeys(label);
        getLabelsRootElement().find(ByUtils.partialLinkText(label)).shouldBe(visible).click();
    }

    public void deleteLabel(String label) {
        Optional<SelenideElement> first =
            getLabelsRootElement().findAll(Element.LABEL_ELEMENT).stream().filter(e -> e.getText().contains(label)).findFirst();
        first.get().find(Element.DELETE_LABEL_BUTTON).click();
    }

    public List<String> getAllLabels() {
        return getLabelsRootElement().findAll(Element.LABEL_ELEMENT).stream().map(SelenideElement::text).collect(Collectors.toList());
    }

    public void addEnvironmentVariable(String env, String value) {
        int numberOfEnvs = getNumberOfEnvs();
        getEnvsRootElement().find(Element.NEW_ENV_BUTTON).shouldBe(visible).click();
        getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_NAME, numberOfEnvs))).sendKeys(env);
        getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_VALUE, numberOfEnvs))).sendKeys(value);
    }

    public void deleteEnvironmentVariable(String env) {
        getRootElement().find(By.id(String.format(Element.CONTROLS_PANEL, getEnvPosition(env)))).find(Element.DELETE_ENV_BUTTON)
            .shouldBe(visible).click();
    }

    public void updateEnvironmentVariable(String integrationEnv, String newValue) {
        SelenideElement input =
            getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_VALUE, getEnvPosition(integrationEnv))));
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
        input.sendKeys(Keys.BACK_SPACE);
        input.clear();
        input.sendKeys(newValue);
    }

    public void updateEnvironmentName(String integrationEnv, String newName) {
        SelenideElement input =
            getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_NAME, getEnvPosition(integrationEnv))));
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
        input.sendKeys(Keys.BACK_SPACE);
        input.clear();
        input.sendKeys(newName);
    }

    public Map<String, String> getAllEnvironmentVariablesAndTheyValues() {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < getNumberOfEnvs(); i++) {
            result.put(getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_NAME, i))).getAttribute("value"),
                getRootElement().find(ByUtils.containsDataTestId(String.format(Input.NEW_ENV_VALUE, i))).getAttribute("value"));
        }
        return result;
    }

    private int getNumberOfEnvs() {
        return getEnvsRootElement().findAll(By.className("form-array-section")).size();
    }

    private int getEnvPosition(String env) {
        String dataTestId = getEnvsRootElement().find(ByUtils.customAttribute("value", env)).getAttribute("data-testid");
        if (StringUtils.substringBetween(dataTestId, "environment-", "-key") == null) {
            Assertions.fail("There is not env with name " + env);
        }
        return Integer.parseInt(Objects.requireNonNull(StringUtils.substringBetween(dataTestId, "environment-", "-key")));
    }
}
