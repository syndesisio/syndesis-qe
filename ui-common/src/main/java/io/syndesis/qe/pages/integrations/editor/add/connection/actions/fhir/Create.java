package io.syndesis.qe.pages.integrations.editor.add.connection.actions.fhir;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Create {

    private static final class Element {
        public static final By RESOURCE_TYPE = ByUtils.dataTestId("resourcetype");
        public static final By CONTAINED_RESOURCE_TYPES = ByUtils.dataTestId("containedresourcetypes");
    }

    public void selectResourceType(String resourceType) {
        log.debug("selecting FHIR resource type: {}", resourceType);
        SelenideElement selectElement = $(Element.RESOURCE_TYPE).shouldBe(visible);
        selectElement.selectOption(resourceType);
    }

    public void selectContainedResourceTypes(List<String> resourceTypes) {
        log.debug("selecting FHIR resource type: {}", resourceTypes.size());
        SelenideElement selectElement = $(Element.CONTAINED_RESOURCE_TYPES).shouldBe(visible);
        resourceTypes.stream().forEach(type -> selectElement.selectOption(type));
    }
}
