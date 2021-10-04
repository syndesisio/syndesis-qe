package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
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
    }

    private static final class Input {
        public static final By NAME = ByUtils.dataTestId("input", "name");
        public static final By DESCRIPTION = By.cssSelector("textarea[name='description']");
        public static final By NEW_LABEL = By.className("pf-c-select__toggle-typeahead");
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
}
