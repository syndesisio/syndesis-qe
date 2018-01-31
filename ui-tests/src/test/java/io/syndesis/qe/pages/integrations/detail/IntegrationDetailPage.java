package io.syndesis.qe.pages.integrations.detail;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationDetailPage extends SyndesisPageObject {

	public final List<String> actionsSet = new ArrayList<String>();

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integration-detail-page");
		public static final By STATUS = By.cssSelector("syndesis-integration-status");
		public static final By TITLE = By.cssSelector("h1");
	}

	private IntegrationDetailPageFactory detailPageFactory = new IntegrationDetailPageFactory();
	private IntegrationDetailPage detailPage;

	public SelenideElement getRootElement() {
		SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
		return elementRoot;
	}

	public boolean validate() {
		return getRootElement().is(visible);
	}

	public String getIntegrationName() {
		return this.getElementText(Element.TITLE);
	}

	public void deleteIntegration() {
		this.getButton(IntegrationDetailActions.DELETE).shouldBe(visible).click();
		this.getButton("OK").shouldBe(visible).click();
	}

	public void editIntegration() {
		this.getButton(IntegrationDetailActions.EDIT).shouldBe(visible).click();
	}

	public void done() {
		this.getButton("Done").shouldBe(visible).click();
	}

	public String getStatus() {
		return this.getElementText(Element.STATUS);
	}

	public void performAction(String action) {
		if (this.actionsSet.contains(action)) {
			this.getButton(action).shouldBe(visible).click();
		} else {
			log.error("Action {} is not available on detail page!", action);
		}
	}

	public SelenideElement getActionButton(String action) {
		return this.getButton(action);
	}

	public IntegrationDetailPage getDetailPage(String integrationStatus) {
		detailPage = detailPageFactory.getDetailPage(integrationStatus);
		return detailPage;
	}
}
