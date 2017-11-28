package io.syndesis.qe.pages.integrations.edit.steps;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicFilterStepComponent extends StepComponent {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-basic-filter");
	}

	private static final class Input {
		public static final By PATH = By.cssSelector("input[name='path']");
		public static final By VALUE = By.cssSelector("input[name='value']");
	}

	private static final class Select {
		public static final By PREDICATE = By.cssSelector("select[id='predicate']");
		public static final By OP = By.cssSelector("select[name='op']");
	}

	private static final class Link {
		public static final By ADD_RULE = By.cssSelector("a.add-rule");
	}

	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	private String filterCondition;

	private String predicateString;

	List<BasicFilterRule> ruleArray;

	public BasicFilterStepComponent(String filterCondition) {
		super();
		this.filterCondition = filterCondition;
		String[] filterConditionsArray = this.filterCondition.split(", ");

		this.predicateString = filterConditionsArray[0];

		this.ruleArray = new ArrayList<BasicFilterRule>();

		for (int i = 1; i < (filterConditionsArray.length - 2); i = i + 3) {
			BasicFilterRule basicFilterRule = new BasicFilterRule(filterConditionsArray[i], filterConditionsArray[i + 1], filterConditionsArray[i + 2]);
			this.ruleArray.add(basicFilterRule);
		}
	}

	public String getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}

	public String getPredicateString() {
		return predicateString;
	}

	public void setPredicateString(String predicate) {
		this.predicateString = predicate;
	}

	public List<BasicFilterRule> getRuleArray() {
		return ruleArray;
	}

	public void setRuleArray(List<BasicFilterRule> ruleArray) {
		this.ruleArray = ruleArray;
	}
	
	public SelenideElement getPredicateSelect() {
		log.debug("Searching basic filter predicate select");
		return this.getRootElement().find(Select.PREDICATE);
	}

	public SelenideElement getPathInput() {
		log.debug("Searching basic filter path input");
		return this.getRootElement().find(Input.PATH);
	}

	public ElementsCollection getPathInputs() {
		log.debug("Searching basic filter path input");
		return this.getRootElement().findAll(Input.PATH);
	}	  

	public SelenideElement getValueInput() {
		log.debug("Searching basic filter value input");
		return this.getRootElement().find(Input.VALUE);
	}
	
	public ElementsCollection getValueInputs() {
		log.debug("Searching basic filter value input");
		return this.getRootElement().findAll(Input.VALUE);
	}
	
	public SelenideElement getOpSelect() {
		log.debug("Searching basic filter op select");
		return this.getRootElement().find(Select.OP);
	}
	
	public ElementsCollection getOpSelects() {
		log.debug("Searching basic filter op selects");
		return this.getRootElement().findAll(Select.OP);
	}

	public void fillConfiguration() {

		for (int i = 0; i < getRuleArray().size(); i++) {
			BasicFilterRule rule = ruleArray.get(i); 
			
			this.setLatestPathInput(rule.getPath());
			this.setLatestOpSelect(rule.getOp());
			this.setLatestValueInput(rule.getValue());

			if (i != (getRuleArray().size() - 1)) {
				SelenideElement addRuleLink = this.getRootElement().find(Link.ADD_RULE);
				addRuleLink.shouldBe(visible).click();
			}
		}

		String predicate = getPredicateString();
		this.setPredicate(predicate);
	}

	public boolean validate() {
		log.info("Validating configuration page");

		SelenideElement predicateSelect = this.getPredicateSelect();
		SelenideElement pathInput = this.getPathInput();
		SelenideElement valueInput = this.getValueInput();
		SelenideElement opSelect = this.getOpSelect();

		boolean isPredicateSelect = predicateSelect.is(visible);
		boolean isPathInput = pathInput.is(visible);
		boolean isValueInput = valueInput.is(visible);
		boolean isOpSelect= opSelect.is(visible);
	    
		log.info("isPredicateSelect {}, isPathInput {}, isValueInput {}, isOpSelect {}", isPredicateSelect , isPathInput , isValueInput , isOpSelect);
		
		return isPredicateSelect && isPathInput && isValueInput && isOpSelect;
	}

	public void initialize() {
		String predicateSelectValue = this.getPredicateSelectValue();

		List<String> pathInputValues = this.getPathInputAllValues();
		List<String> opSelectValues = this.getOpSelectAllValues();
		List<String> valueInputValues = this.getValueInputAllValues();

		String parameter = predicateSelectValue;

		this.ruleArray = new ArrayList<BasicFilterRule>();

		for (int i = 0; i < pathInputValues.size(); i++) {
			BasicFilterRule basicFilterRule = new BasicFilterRule(pathInputValues.get(i), opSelectValues.get(i), valueInputValues.get(i));
			this.ruleArray.add(basicFilterRule);

			parameter = parameter + ", " + pathInputValues.get(i) + ", " + opSelectValues.get(i) + ", " + valueInputValues.get(i);
		}

		this.setParameter(parameter);
	}

	public void addRule(String ruleString) {
		String[] ruleStringArray = ruleString.split(", ");
		BasicFilterRule basicFilterRule = new BasicFilterRule(ruleStringArray[0], ruleStringArray[1], ruleStringArray[2]);

		SelenideElement addRuleLink = this.getRootElement().$(Link.ADD_RULE);
		addRuleLink.shouldBe(visible).click();

		this.setLatestPathInput(basicFilterRule.getPath());
		this.setLatestOpSelect(basicFilterRule.getOp());
		this.setLatestValueInput(basicFilterRule.getValue());

		this.ruleArray.add(basicFilterRule);
	}

	public void setParameter(String filterCondition) {
		this.setFilterCondition(filterCondition);
	}

	public void setPredicate(String predicate) {
		log.info("setting basic filter step predicate to option number {}", predicate);
		SelenideElement predicateInput = this.getRootElement().find(Select.PREDICATE);

		this.selectOption(predicateInput, predicate);
	}

	public void setOp(String op) {
		log.info("setting basic filter step op to option number {}", op);
		SelenideElement opInput = this.getRootElement().find(Select.OP);

		this.selectOption(opInput, op);
	}

	public void setPath(String path) {
		log.info("setting basic filter step path to {}", path);
		SelenideElement pathInput = this.getRootElement().find(Input.PATH);

		pathInput.shouldBe(visible).clear();
		pathInput.shouldBe(visible).sendKeys(path);
	}

	public void setValue(String value) {
		log.info("setting basic filter step value to {}", value);
		SelenideElement valueInput = this.getRootElement().find(Input.VALUE);

		valueInput.shouldBe(visible).clear();
		valueInput.shouldBe(visible).sendKeys(value);
	}

	public void setLatestOpSelect(String op) {
		log.info("setting basic filter step op to option number {}", op);
		ElementsCollection opInputArray = this.getRootElement().findAll(Select.OP);
		SelenideElement opInput = opInputArray.get(opInputArray.size() - 1);

		this.selectOption(opInput, op);
	}

	public void setLatestPathInput(String path) {
		log.info("setting basic filter step path to {}", path);
		ElementsCollection pathInputArray = this.getRootElement().findAll(Input.PATH);
		SelenideElement pathInput = pathInputArray.get(pathInputArray.size() - 1);
		
		pathInput.shouldBe(visible).clear();
		pathInput.shouldBe(visible).sendKeys(path);
	}

	public void  setLatestValueInput(String value) {
		log.info("Setting basic filter step value to {}", value);
		ElementsCollection valueInputArray = this.getRootElement().findAll(Input.VALUE);
		SelenideElement valueInput = valueInputArray.get(valueInputArray.size() - 1);

		valueInput.shouldBe(visible).clear();
		valueInput.shouldBe(visible).sendKeys(value);
	}

	public String getParameter() {
		return this.getFilterCondition();
	}

	public String getPredicateSelectValue() {
		log.debug("Searching basic filter predicate select checked option");
		String predicateValue = this.getPredicateSelect().shouldBe(visible).getText();
		return predicateValue.trim();
	}
	
	public String getPathInputValue() {
		return this.getPathInput().getAttribute("value");
	}

	public List<String> getPathInputAllValues() {
		ElementsCollection pathInputArray = this.getPathInputs();
		int count = pathInputArray.size();

		List<String> pathInputValues = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			String value = pathInputArray.get(i).getAttribute("value");
			pathInputValues.add(value);
		}

		return pathInputValues;
	}


	public String getValueInputValue() {
		return this.getValueInput().getAttribute("value");
	}

	public List<String> getValueInputAllValues() {
		ElementsCollection valueInputArray = this.getValueInputs();
		int count = valueInputArray.size();

		List<String> valueInputValues = new ArrayList<String>();

		for (int i = 0; i < count; i++) {
			String value = valueInputArray.get(i).getAttribute("value");
			valueInputValues.add(value);
		}

		return valueInputValues;
	}

	public String getOpSelectValue() {
		log.debug("Searching basic filter op select checked option");
		String opValue = this.getOpSelect().shouldBe(visible).getText();
		return opValue.trim();
	}

	public List<String> getOpSelectAllValues() {
		log.debug("Searching basic filter op select checked options");

		ElementsCollection opSelectArray = this.getOpSelects();
		int size = opSelectArray.size();

		List<String> opSelectValues = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			String value = opSelectArray.get(i).shouldBe(visible).getText();
			opSelectValues.add(value.trim());
		}

		return opSelectValues;
	}
}
