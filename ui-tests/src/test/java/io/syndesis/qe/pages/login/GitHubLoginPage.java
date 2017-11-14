package io.syndesis.qe.pages.login;

import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

public class GitHubLoginPage implements LoginPage {
	private static final class Button {
		public static final By SIGNIN = By.xpath("//input[@type='submit']");
	}

	private static final class Input {
		public static final By USERNAME = By.id("login_field");
		public static final By PASSWORD = By.id("password");
	}

	@Override
	public void login(String username, String password) {
		$(Input.USERNAME).shouldBe(Condition.visible).setValue(username);
		$(Input.PASSWORD).shouldBe(Condition.visible).setValue(password);
		$(Button.SIGNIN).shouldBe(Condition.visible).click();
	}
}
