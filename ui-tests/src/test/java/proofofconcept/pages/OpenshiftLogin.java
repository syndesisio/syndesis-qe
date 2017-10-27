package proofofconcept.pages;

import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

public class OpenshiftLogin {
	private static final class Button {
		public static final By SIGNIN = By.xpath("//button[@type='submit']");
	}

	private static final class Text {
		public static final By USERNAME = By.id("inputUsername");
	}

	private static final class Password {
		public static final By PASSWORD = By.id("inputPassword");
	}

	public void logIn(String username, String password) {
		$(Text.USERNAME).shouldBe(Condition.visible).setValue(username);
		$(Password.PASSWORD).shouldBe(Condition.visible).setValue(password);
		$(Button.SIGNIN).shouldBe(Condition.visible).click();
	}
}
