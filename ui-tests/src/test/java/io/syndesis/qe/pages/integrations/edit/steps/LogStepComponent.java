package io.syndesis.qe.pages.integrations.edit.steps;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogStepComponent extends StepComponent {

	private static final class Input {
		public static final By NAME = By.cssSelector("input[name='message']");
	}

	private String logMessage;

	public LogStepComponent(String logMessage) {
		super();
		this.logMessage = logMessage;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public void fillConfiguration() {
		String message = this.getLogMessage();
		this.setMessage(message);
	}

	public boolean validate() {
		log.debug("validating configuration page");
		return this.getMessageInput().is(visible);
	}

	public void initialize() {
		String text =  this.getMessageInputValue();
		this.setParameter(text);
	}

	public void setMessage(String message) {
		log.info(String.format("setting integration step message to ", message));
		this.getRootElement().find(Input.NAME).shouldBe(visible).sendKeys(message);
	}

	public void setParameter(String logMessage) {
		this.setLogMessage(logMessage);
	}

	public SelenideElement getMessageInput() {
		log.debug("searching for message input");
		return this.getRootElement().find(Input.NAME);
	}

	public String getMessageInputValue() {
		return this.getMessageInput().getAttribute("value");
	}

	public String getParameter() {
		return this.getLogMessage();
	}
}
