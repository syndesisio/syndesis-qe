package io.syndesis.qe.pages.integrations.edit.steps;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvancedFilterStepComponent extends StepComponent {

	private static final class Textarea {
		public static final By FILTER = By.cssSelector("textarea[name='filter']");
	}

	private String filterString;

	public AdvancedFilterStepComponent(String filterString) {
		super();
		this.filterString = filterString;
	}

	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	public void fillConfiguration() {
		String filter = getFilterString();
		this.setFilter(filter);
	}

	public boolean validate() {
		log.debug("Validating advanced filter configuration page");
		return this.getFilterTextarea().waitWhile(Condition.not(visible), 5 * 1000).isDisplayed();
	}

	public void initialize() {
		String filter = this.getFilterTextareaValue();
		this.setParameter(filter);
	}

	public void setFilter(String filter) {
		log.info("Setting integration step filter to {}", filter);
		this.getFilterTextarea().shouldBe(visible).sendKeys(filter);
	}

	public void setParameter(String filterString) {
		setFilterString(filterString);
	}

	public SelenideElement getFilterTextarea() {
		log.debug("Searching for filter text area");
		return this.getRootElement().find(Textarea.FILTER);
	}

	public String getFilterTextareaValue() {
		return this.getFilterTextarea().shouldBe(visible).getText();
	}

	public String getParameter() {
		return this.filterString;
	}	
}
