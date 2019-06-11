package io.syndesis.qe.pages.integrations.editor.add.steps;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.switchTo;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/14/17.
 */
@Slf4j
public class DataMapper extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("data-mapper");
        public static final By LOADER_SELECTOR = By.cssSelector("div.card-pf-heading.fieldsCount");
        public static final By DM_COLLUMNS = By.cssSelector("div.docDef");
        public static final By LABEL = By.cssSelector("label");
        public static final By NAME = By.cssSelector("div.fieldDetail > div > label");
        public static final By PARENT = By.cssSelector("div.parentField");
        public static final By CHILDREN = By.cssSelector("div.childrenFields");
        public static final By FIRST_SOURCE = By.cssSelector("div:nth-child(1) > mapping-field-detail > div > div > input.ng-untouched.ng-pristine.ng-valid");
        public static final By SECOND_SOURCE = By.cssSelector("div:nth-child(2) > mapping-field-detail > div > div > input.ng-untouched.ng-pristine.ng-valid");
        public static final By FIRST_TARGET = By.cssSelector("simple-mapping:nth-child(6) > div > div:nth-child(1) > mapping-field-detail > div > div > input");
        public static final By SECOND_TARGET = By.cssSelector("simple-mapping:nth-child(6) > div > div:nth-child(2) > mapping-field-detail > div > div > input");
        public static final By FIRST_SOURCE_POSITION = By.xpath("(//mapping-field-action//label[text()='Index']/following-sibling::input)[1]");
        public static final By SECOND_SOURCE_POSITION = By.xpath("(//mapping-field-action//label[text()='Index']/following-sibling::input)[2]");
        public static final By FIRST_TARGET_POSITION = By.cssSelector("simple-mapping:nth-child(6) > div > div:nth-child(1) > mapping-field-action > div > div.actionContainer > div.form-group.argument > input");
        public static final By SECOND_TARGET_POSITION = By.cssSelector("simple-mapping:nth-child(6) > div > div:nth-child(2) > mapping-field-action > div > div > div.form-group.argument > input");
        public static final By ACTION_SELECT = By.xpath("//select[@id='selectAction']");
        public static final By SEPARATOR_SELECT = By.xpath("//select[@id='select-separator']");
        public static final By TRANSFORMATION_SELECT = By.xpath("//label[text() = 'Transformation']/following-sibling::select");
        public static final By BUCKET_IS_IN_OPEN_STATE = By.className("panel-collapse");
        public static final By CONSTANTS_BUCKET = By.id("Constants");
        public static final By PROPERTIES_BUCKET = By.id("Properties");
        public static final By MODAL_WINDOW = By.id("modalWindow");
        public static final By CONSTANT_FIELD_EDIT_ELEMENT = By.tagName("constant-field-edit");
        public static final By PROPERTY_FIELD_EDIT_ELEMENT = By.tagName("property-field-edit");
        public static final By CONSTANT_NAME_FIELD = By.id("name");
        public static final By PROPERTY_VALUE_FIELD = By.id("value");
        public static final By CONSTANT_TYPE_SELECT = By.cssSelector("select");
        public static final By PRIMARY_BUTTON = By.cssSelector("button.btn-primary");
        public static final By MAPPER_COLLECTION_ICON = By.className("parentField");
        public static final By ADD_MAPPING_ICON = By.className("fa-plus");
        public static final By ATLASMAP_IFRAME = By.cssSelector("iframe[name=\"atlasmap-frame\"]");
        public static final By ARROW_POINTING_RIGHT = By.cssSelector("i.arrow.fa.fa-angle-right");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ATLASMAP_IFRAME).is(visible);
    }

    /**
     * Find proper source and target datamapper columns.
     *
     * @returns ElementsCollection div elements
     */
    private ElementsCollection dataMapperColumns() {
        log.info("searching for columns");
        //loadSelector should be visible:
        $(Element.LOADER_SELECTOR).shouldBe(visible);
        log.info("datamapper has been loaded");
        ElementsCollection dmColumns = this.getRootElement().findAll(Element.DM_COLLUMNS).shouldBe(size(2));
        log.info("found {} datamapper columns", dmColumns.size());
        return dmColumns;
    }

    /**
     * Eventually returns count of found datamapper fields.
     *
     * @returns count of fields
     */
    public Integer fieldsCount() {
        ElementsCollection dmColumns = this.dataMapperColumns();
        SelenideElement countElement = dmColumns.get(0).$(Element.LOADER_SELECTOR).shouldBe(visible);
        String countText = countElement.getText();
        // "77 fields" -> 77
        String[] found = countText.split(" ");
        if (found.length != 2) {
            throw new IllegalArgumentException(String.format("failed to get files number from %s", countText));
        }
        return Integer.parseInt(found[0]);
    }


    /**
     * This method can create all types of data mapper mappings.
     * <p>
     * If you want to combine or separate functions, just use it as in this example:
     * <p>
     * Basic:           createMapping("user", "firstName")
     * Combine:         createMapping("user; address", "description")
     * Separate:        createMapping("name", "firstName; lastName")
     * <p>
     * For combine and separate, data mapper will automatically use default separator - space. Separator setting is not
     * implemented yet because it was not needed.
     *
     * @param source
     * @param target
     */
    public void createMapping(String source, String target) {
        doCreateMapping(source, target);
    }

    /**
     * This method opens all collection mappings we can find on a data mapper
     */
    public void openDataMapperCollectionElement() {
        $$(Element.MAPPER_COLLECTION_ICON).forEach(e -> {
            SelenideElement arrow = e.find(Element.ARROW_POINTING_RIGHT);
            if (arrow.exists() && arrow.is(visible)) {
                arrow.click();
            }
        });
    }

    /**
     * This method opens only unnamed collection mappings we can find on a data mapper
     */
    public void openDataMapperUnnamedCollectionElement() {
        $$(Element.MAPPER_COLLECTION_ICON).forEach(e -> {
            // if there is label on the collection, which is empty
            // or if label does not exist, collection is unnamed
            SelenideElement label = e.find("label");
            if (!label.exists() || label.text().isEmpty()) {
                SelenideElement arrow = e.find(Element.ARROW_POINTING_RIGHT);
                if (arrow.exists() && arrow.is(visible)) {
                    arrow.click();
                }
            }
        });
    }

    public void doCreateMapping(String source, String target) {
        createNewMapping(source, target);
        $(Element.ADD_MAPPING_ICON).scrollIntoView(true).shouldBe(visible).click();
    }

    private void createNewMapping(String source, String target) {
        ElementsCollection dmColumns = this.dataMapperColumns();
        SelenideElement src = dmColumns.get(0);
        SelenideElement dest = dmColumns.get(1);

        if (source.contains(";")) {
            String[] array = source.split(";");
            for (String str : array) {
                this.selectMapping(str.trim(), src);
            }
            this.selectMapping(target, dest);

        } else if (target.contains(";")) {
            this.selectMapping(source, src);

            String[] array = target.split(";");
            for (String str : array) {
                this.selectMapping(str.trim(), dest);
            }
        } else {
            this.selectMapping(source, src);
            this.selectMapping(target, dest);
        }
    }

    public void doCreateMappingWithSeparator(String source, String target, String separator) {
        createNewMapping(source, target);
        $(By.id("select-separator")).shouldBe(visible).selectOptionContainingText(separator);
        $(Element.ADD_MAPPING_ICON).shouldBe(visible).click();
    }

    /**
     * @param mappingName      for instance "User.ScreenName"
     * @param containerElement start searching mapping fields from here
     */
    public void selectMapping(String mappingName, SelenideElement containerElement) {
        //split and trim in one step:
        List<String> path = Arrays.asList(mappingName.trim().split("\\."));
        final String parent = path.get(0);

        path.forEach(s -> {
            SelenideElement detailElement = containerElement.find(By.id(s)).shouldBe(visible);
            if (detailElement.find(Element.CHILDREN).exists()) {
                // if there're childrenFields display element is expanded already, click otherwise
            } else {
                SelenideElement el;
                if (!s.equals(parent)) {
                    el = containerElement.find(By.id(parent)).find(By.id(s)).$(Element.LABEL).shouldBe(visible);
                } else {
                    el = detailElement.$(Element.LABEL).shouldBe(visible);
                }
                el.scrollIntoView(true);
                TestUtils.sleepIgnoreInterrupt(500);

                new Actions(WebDriverRunner.getWebDriver())
                        .moveToElement(el)
                        .keyDown(Keys.META)
                        .click()
                        .keyUp(Keys.META)
                        .perform();

            }

        });
    }

    /**
     * Get string name from given datamapper field element.
     *
     * @param fieldElement element to capture name
     * @returns String field name
     */
    public String fieldName(SelenideElement fieldElement) {
        SelenideElement nameElement = fieldElement.$(Element.NAME).shouldBe(visible);
        return nameElement.getText();
    }

    public void fillInputAndConfirm(SelenideElement element, String value) {
        element.shouldBe(visible).clear();
        element.shouldBe(visible).sendKeys(value);
        //TODO: this is pretty ugly sleep should be addressed in future
        Selenide.sleep(5 * 1000);
        Selenide.actions().sendKeys(Keys.ENTER).perform();
    }

    public SelenideElement getElementByAlias(String alias) {

        By locator;

        switch (alias) {
            case "FirstSource": {
                locator = Element.FIRST_SOURCE;
                break;
            }
            case "SecondSource": {
                locator = Element.SECOND_SOURCE;
                break;
            }
            case "FirstTarget": {
                locator = Element.FIRST_TARGET;
                break;
            }
            case "SecondTarget": {
                locator = Element.SECOND_TARGET;
                break;
            }
            case "FirstSourcePosition": {
                locator = Element.FIRST_SOURCE_POSITION;
                break;
            }
            case "SecondSourcePosition": {
                locator = Element.SECOND_SOURCE_POSITION;
                break;
            }
            case "FirstTargetPosition": {
                locator = Element.FIRST_TARGET_POSITION;
                break;
            }
            case "SecondTargetPosition": {
                locator = Element.SECOND_TARGET_POSITION;
                break;
            }
            case "ActionSelect": {
                locator = Element.ACTION_SELECT;
                break;
            }
            case "SeparatorSelect": {
                locator = Element.SEPARATOR_SELECT;
                break;
            }
            case "TransformationSelect": {
                locator = Element.TRANSFORMATION_SELECT;
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Alias %s doesnt exist", alias));
            }
        }

        return this.getElementByLocator(locator);
    }

    public SelenideElement getDataBucketElement(String bucketName) {
        return $(By.id(bucketName)).shouldBe(visible);
    }

    public void openBucket(String bucketName) {
        SelenideElement bucket = getDataBucketElement(bucketName);
        if (!bucket.$(Element.BUCKET_IS_IN_OPEN_STATE).isDisplayed()) {
            bucket.click();
            getRootElement().hover();
        }
    }

    public void closeBucket(String bucketName) {
        SelenideElement bucket = getDataBucketElement(bucketName);
        if (bucket.$(Element.BUCKET_IS_IN_OPEN_STATE).isDisplayed()) {
            bucket.click();
            getRootElement().hover();
        }
    }

    public void addConstant(String value, String type) {
        SelenideElement constantsDiv = getRootElement().$(Element.CONSTANTS_BUCKET).shouldBe(visible);
        SelenideElement plusIcon = constantsDiv.$(By.cssSelector("i.link")).shouldBe(visible);
        plusIcon.shouldBe(visible).click();
        SelenideElement dialog = getRootElement().$(Element.MODAL_WINDOW).shouldBe(visible);
        SelenideElement constantFieldEdit = dialog.$(Element.CONSTANT_FIELD_EDIT_ELEMENT).shouldBe(visible);
        constantFieldEdit.$(Element.CONSTANT_NAME_FIELD).shouldBe(visible).sendKeys(value);
        SelenideElement select = constantFieldEdit.$(Element.CONSTANT_TYPE_SELECT).shouldBe(visible);
        select.selectOption(type);
        dialog.$(Element.PRIMARY_BUTTON).shouldBe(visible).click();
    }

    public void addProperty(String name, String value, String type) {
        SelenideElement constantsDiv = getRootElement().$(Element.PROPERTIES_BUCKET).shouldBe(visible);
        SelenideElement plusIcon = constantsDiv.$(By.cssSelector("i.link")).shouldBe(visible);
        plusIcon.shouldBe(visible).click();
        SelenideElement dialog = getRootElement().$(Element.MODAL_WINDOW).shouldBe(visible);
        SelenideElement constantFieldEdit = dialog.$(Element.PROPERTY_FIELD_EDIT_ELEMENT).shouldBe(visible);
        constantFieldEdit.$(Element.CONSTANT_NAME_FIELD).shouldBe(visible).sendKeys(name);
        constantFieldEdit.$(Element.PROPERTY_VALUE_FIELD).shouldBe(visible).sendKeys(value);
        SelenideElement select = constantFieldEdit.$(Element.CONSTANT_TYPE_SELECT).shouldBe(visible);
        select.selectOption(type);
        dialog.$(Element.PRIMARY_BUTTON).shouldBe(visible).click();
    }

    /**
     * Switch driver context to data-mapper iframe. This is necessary because of all external components
     * that are't coded in react (Datamapper, apicurito) are paced in different iframes.
     */
    public void switchToDatamapperIframe() {
        switchTo().frame($(Element.ATLASMAP_IFRAME).shouldBe(visible).toWebElement());
    }

    /**
     * Switch driver context back to syndesis iframe.
     * This is necessary becasue all external components that are't coded in react (Datamapper, apicurito) are paced in different iframes.
     */
    public void switchIframeBack() {
        switchTo().defaultContent();
    }
}
