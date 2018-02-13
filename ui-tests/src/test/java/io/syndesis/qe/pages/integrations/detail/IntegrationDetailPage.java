package io.syndesis.qe.pages.integrations.detail;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.io.File;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class IntegrationDetailPage extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-integration-detail-page");
		public static final By STATUS = By.cssSelector("syndesis-integration-status");
		public static final By TITLE = By.cssSelector("h1");
	}
	
	public static final class Status {
		public static final String ACTIVE = "Active";
		public static final String INACTIVE = "Inactive";
		public static final String DRAFT = "Draft";
		public static final String IN_PROGRESS = "In Progress";
	}

	public static final class Actions {
		public static final String START = "Start Integration";
		public static final String STOP = "Stop Integration";
		public static final String DELETE = "Delete Integration";
		public static final String EDIT = "Edit Integration";
	}

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
		this.getButton(Actions.DELETE).shouldBe(visible).click();
		this.getButton("OK").shouldBe(visible).click();
	}

	public void editIntegration() {
		this.getButton(Actions.EDIT).shouldBe(visible).click();
	}

	public void done() {
		this.getButton("Done").shouldBe(visible).click();
	}

	public void toggleIntegrationState() {
		String status = getStatus();
		
		if (status.equals(Status.DRAFT) || status.equals(Status.INACTIVE)) {
			this.getButton(Actions.START).shouldBe(visible).click();
			this.getButton("OK").shouldBe(visible).click();
		} else if (status.equals(Status.ACTIVE)) {
			this.getButton(Actions.STOP).shouldBe(visible).click();
			this.getButton("OK").shouldBe(visible).click();
		} else {
			log.error("Integration state {} cant be toggled!", status);
		}
	}

	public String getStatus() {
		return this.getElementText(Element.STATUS);
	}

	public SelenideElement getActionButton(String action) {
		return this.getButton(action);
	}

	public File exportIntegration() throws InterruptedException {

		this.getButton("Export").shouldBe(visible).click();
		String filePath = CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + this.getIntegrationName() + "-export.zip";

		// wait for download
		Thread.sleep(5000);

		return new File(filePath);
	}


}
