package io.syndesis.qe.pages.connections;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionListPage extends SyndesisPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-connections-list-page");
	}

	private ConnectionsListComponent listComponent = new ConnectionsListComponent();

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(Condition.visible);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	//why everytime new instance? (return new new ConnectionsListComponent())
	public ConnectionsListComponent listComponent() {
		return this.listComponent;
	}
}
