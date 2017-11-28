package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponentFactory;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponent;

public class IntegrationFlowViewComponent extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-flow-view");

		public static final By NAME = By.cssSelector("input.form-control.integration-name");
		public static final By STEP = By.cssSelector("div.parent-step");
		public static final By ACTIVE_STEP = By.cssSelector("div[class='parent-step active']");
		//DELETE AND DELETE ARE IDENTICAL
		public static final By DELETE = By.className("delete-icon");
	}

	private StepComponentFactory stepComponentFactory = new StepComponentFactory();

	@Override
	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	@Override
	public boolean validate() {
		return getRootElement().is(visible);
	}

	public String getIntegrationName() {
		return this.getRootElement().find(Element.NAME).shouldBe(visible).getAttribute("value");
	}

	/**
	 * Get div
	 *
	 * @param type (start|finish)
	 */
	public FlowConnection flowConnection(String type) {
		SelenideElement stepElement = this.getElementContainingText(Element.STEP, type).shouldBe(visible);

		type = type.toLowerCase();
		return new FlowConnection(type, stepElement);
	}

	public List<String> getStepsArray() {
		ElementsCollection steps = this.getRootElement().findAll(Element.STEP);

		List<String> stepsArray = new ArrayList<String>();

		for (int i = 1; i < (steps.size() - 1); i++) {
			steps.get(i).click();

			SelenideElement title = this.getRootElement().find(Element.ACTIVE_STEP);

			String type = title.getText();
			StepComponent stepComponent = stepComponentFactory.getStep(type, "");

			//wait for root element to be loaded
			stepComponent.getRootElement();
			stepComponent.initialize();

			stepsArray.add(stepComponent.getParameter());
		}

		this.clickOnFirstVisibleButton("Done");

		return stepsArray;
	}

	public ElementsCollection getAllTrashes() {
		return this.getRootElement().findAll(Element.DELETE);
	}

	public void clickRandomTrash() {
		this.clickElementRandom(Element.DELETE);
	}
}
