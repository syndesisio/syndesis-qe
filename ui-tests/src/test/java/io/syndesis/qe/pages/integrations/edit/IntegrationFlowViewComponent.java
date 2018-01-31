package io.syndesis.qe.pages.integrations.edit;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponent;
import io.syndesis.qe.pages.integrations.edit.steps.StepComponentFactory;

public class IntegrationFlowViewComponent extends SyndesisPageObject {

	private static final class Link {
		public static final By ADD_STEP = By.linkText("Add a Step");
	}

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integrations-flow-view");

		public static final By NAME = By.cssSelector("input.form-control.integration-name");
		public static final By STEP_ROOT = By.cssSelector("div.flow-view-step");
		public static final By STEP = By.cssSelector("div.parent-step");
		public static final By ACTIVE_STEP = By.cssSelector("div[class='parent-step active']");
		public static final By ACTIVE_STEP_ICON = By.cssSelector("p.icon.active");
		public static final By DELETE = By.className("delete-icon");
		public static final By STEP_INSERT = By.className("step-insert");

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
	 * Check if there's an icon in active state goToNextWizardStep to the position in the integration flow
	 *
	 * @param position (start|finish)
	 */
	public boolean verifyActivePosition(String position) {
		SelenideElement selenideElement = getRootElement().find(By.cssSelector("div.step." + position.toLowerCase()));
		return selenideElement.find(Element.ACTIVE_STEP_ICON).shouldBe(visible).exists();
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

	public void clickAddStepLink(int pos){

		List<WebElement> allStepInserts = getRootElement().findElements(Element.STEP_INSERT);
		WebElement stepElement = allStepInserts.get(pos);
		Actions action = new Actions(WebDriverRunner.getWebDriver());
		//to make ADD_STEP element visible:
		action.moveToElement(stepElement);

		getRootElement().$(Link.ADD_STEP).shouldBe(visible).click();
	}

}
