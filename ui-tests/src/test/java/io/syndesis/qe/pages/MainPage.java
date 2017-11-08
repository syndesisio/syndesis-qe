package io.syndesis.qe.pages;

import org.openqa.selenium.By;

public abstract class MainPage {

	public static final class Tab {
		public static final By HOME = By.xpath("//div/ul[@class='list-group']/li/a[@href='/dashboard']");
	}

	abstract void checkVisibility(boolean shouldBeVisible);
}
