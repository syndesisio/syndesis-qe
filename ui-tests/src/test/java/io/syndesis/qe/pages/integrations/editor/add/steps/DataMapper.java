package io.syndesis.qe.pages.integrations.editor.add.steps;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.util.Arrays;
import java.util.List;

import io.syndesis.qe.pages.SyndesisPageObject;
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
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    /**
     * Find proper source and target datamapper columns.
     *
     * @returns ElementsCollection div elements
     */
    private ElementsCollection dataMapperColumns() {
        log.info("searching for columns");
        //loadSelector should be visible:
        this.getRootElement().$(Element.LOADER_SELECTOR).shouldBe(visible);
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

    public void createMapping(String source, String target) {
        log.info("creating mapping from {} to {}", source, target);
        ElementsCollection dmColumns = this.dataMapperColumns();
        SelenideElement src = dmColumns.get(0);
        SelenideElement dest = dmColumns.get(1);

        this.selectMapping(source, src);
        this.selectMapping(target, dest);
    }

    /**
     * @param mappingName for instance "User.ScreenName"
     * @param containerElement start searching mapping fields from here
     */
    public void selectMapping(String mappingName, SelenideElement containerElement) {
        //split and trim in one step:
        List<String> path = Arrays.asList(mappingName.trim().split("\\."));

        path.forEach(s -> {
            SelenideElement detailElement = containerElement.find(By.id(s)).shouldBe(visible);
            if (detailElement.find(Element.CHILDREN).exists()) {
                // if there're childrenFields display element is expanded already, click otherwise
            } else {
                detailElement.$(Element.LABEL).shouldBe(visible).click();
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
        return getRootElement().$(By.id(bucketName)).shouldBe(visible);
    }

    public void openBucket(String bucketName) {
        SelenideElement bucket = getDataBucketElement(bucketName);
        if(!bucket.$(Element.BUCKET_IS_IN_OPEN_STATE).isDisplayed()) {
            bucket.click();
            getRootElement().hover();
        }
    }

    public void closeBucket(String bucketName) {
        SelenideElement bucket = getDataBucketElement(bucketName);
        if(bucket.$(Element.BUCKET_IS_IN_OPEN_STATE).isDisplayed()) {
            bucket.click();
            getRootElement().hover();
        }
    }
}
