package pages;

import org.openqa.selenium.By;

/**
 * Created by mastepan on 10/31/17.
 */
public abstract class MainPage {

	public static final class Tab {
		public static final By HOME = By.xpath("//div/ul[@class='list-group']/li/a[@href='/dashboard']");
	}

	abstract void checkVisibility(boolean shouldBeVisible);
}
