package io.syndesis.qe.pages.integrations.detail;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/14/17.
 */
@Slf4j
public class DataMapperComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("data-mapper");
		public static final By LOADER_SELECTOR = By.cssSelector("div.card-pf-heading.fieldsCount");
		public static final By DM_COLLUMNS = By.cssSelector("div.docDef");
		public static final By FIELD_DETAIL = By.cssSelector("document-field-detail");
		public static final By LABEL = By.cssSelector("label");
		public static final By NAME = By.cssSelector("div.fieldDetail > div > label");
		public static final By PARENT = By.cssSelector("div.parentField");
		public static final By CHILDREN = By.cssSelector("div.childrenFields");
		public static final By FIRST_COMBINE = By.cssSelector("div:nth-child(1) > mapping-field-detail > div > div > input.ng-untouched.ng-pristine.ng-valid");
		public static final By SECOND_COMBINE = By.cssSelector("div:nth-child(2) > mapping-field-detail > div > div > input.ng-untouched.ng-pristine.ng-valid");
		public static final By TARGET_COMBINE = By.cssSelector("simple-mapping:nth-child(6)>div>div>mapping-field-detail>div>div>input.ng-untouched.ng-pristine.ng-valid");
		public static final By FIRST_COMBINE_POSITION = By.xpath("(//mapping-field-action//label[text()='Index']/following-sibling::input)[1]");
		public static final By SECOND_COMBINE_POSITION = By.xpath("(//mapping-field-action//label[text()='Index']/following-sibling::input)[2]");
		public static final By ACTION_SELECT = By.xpath("//label[text()='Action']/following-sibling::select");
		public static final By SEPARATOR_SELECT = By.xpath("//label[text()='Separator:']/following-sibling::select");
		public static final By TRANSFORMATION_SELECT = By.xpath("//label[text() = 'Transformation']/following-sibling::select");
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT);
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
	Integer fieldsCount() throws Exception {
		ElementsCollection dmColumns = this.dataMapperColumns();
		SelenideElement countElement = dmColumns.get(0).$(Element.LOADER_SELECTOR).shouldBe(visible);
		String countText = countElement.getText();
		// "77 fields" -> 77
		String[] found = countText.split(" ");
		if (found.length != 2) {
			throw new Exception(String.format("failed to get files number from %s", countText));
		}
		return Integer.parseInt(found[0]);
	}

	void createMapping(String source, String target) throws Exception {
		log.info("creating mapping from {} to {}", source, target);
		ElementsCollection dmColumns = this.dataMapperColumns();
		SelenideElement src = dmColumns.get(0);
		SelenideElement dest = dmColumns.get(1);

		this.selectMapping(source, src);
		this.selectMapping(target, dest);
	}

	/**
	 * Filter datamapper field element by it's name
	 *
	 * @param name name to find
	 * @param fields fields ElementsCollection
	 * @returns SelenideElement field element
	 */
	SelenideElement findFieldByName(String name, ElementsCollection fields) throws Exception {
		log.info("searching field named {}", name);
		for (SelenideElement f : fields) {
			String fieldName = this.fieldName(f);
			if (name.equals(fieldName)) {
				log.info("field {} found", name);
				return f;
			}
		}
		log.warn("field {} not found between {} fields.length fields, rejecting", name, fields.size());
		throw new Exception(String.format("Field %s not found in given %d fields", name, fields.size()));
	}

	/**
	 * @param mappingName for instance "User.ScreenName"
	 * @param containerElement start searching mapping fields from here
	 */
	void selectMapping(String mappingName, SelenideElement containerElement) throws Exception {
		//split and trim in one step:
		String[] path = mappingName.trim().split("\\s*,\\s*");

		ElementsCollection fields = containerElement.findAll(Element.FIELD_DETAIL).shouldBe(sizeGreaterThanOrEqual(1));
		log.info("source has {} fields.length fields", fields.size());

		SelenideElement nextField;
		for (String p : path) {
			nextField = this.findFieldByName(p, fields);
			// click on it to expand or select find correct field from list
			log.info("Clicking on field {}", p);
			nextField.$(Element.LABEL).shouldBe(visible).click();
			// find all subfields for next iteration
			fields = nextField.$$(Element.FIELD_DETAIL).shouldBe(sizeGreaterThanOrEqual(1));
		}
	}

	/**
	 * Get string name from given datamapper field element.
	 *
	 * @param fieldElement element to capture name
	 * @returns String field name
	 */
	String fieldName(SelenideElement fieldElement) {
		SelenideElement nameElement = fieldElement.$(Element.NAME).shouldBe(visible);
		return nameElement.getText();
	}

	/**
	 * Expand field and return list of child elements
	 *
	 * @param field
	 * @returns ElementsCollection list of child elements or empty
	 */
	ElementsCollection expandField(SelenideElement field) {
		//should be parent element:
		field.$(Element.PARENT).shouldBe(visible);
		field.click();
		ElementsCollection children = field.$(Element.CHILDREN).$$(Element.FIELD_DETAIL).shouldBe(sizeGreaterThanOrEqual(1));
		log.info("field {} has {} child fields", field.getText(), children.size());
		return children;
	}

	SelenideElement getElementByAlias(String alias) throws Exception {

		By locator;
		SelenideElement inputElement;

		switch (alias) {
			case "FirstCombine": {
				locator = Element.FIRST_COMBINE;
				break;
			}
			case "SecondCombine": {
				locator = Element.SECOND_COMBINE;
				break;
			}
			case "TargetCombine": {
				locator = Element.TARGET_COMBINE;
				break;
			}
			case "FirstCombinePosition": {
				locator = Element.FIRST_COMBINE_POSITION;
				break;
			}
			case "SecondCombinePosition": {
				locator = Element.SECOND_COMBINE_POSITION;
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
				throw new Exception(String.format("Alias %s doesnt exist", alias));
			}
		}

		return this.getElementByLocator(locator);
	}
}
