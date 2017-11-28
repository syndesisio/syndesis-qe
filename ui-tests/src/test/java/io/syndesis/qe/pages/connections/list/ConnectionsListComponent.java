package io.syndesis.qe.pages.connections.list;


import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionsListComponent extends SyndesisPageObject {

	private static final class Button {
		public static final By DELETE = By.linkText("Delete");

		public static final By OK = By.linkText("OK");
		public static final By KEBAB = By.cssSelector("button.btn.btn-link.dropdown-toggle");
	}

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-connections-list");
		public static final By CONNECTION = By.cssSelector("h2.card-pf-title.text-center");
		public static final By KEBAB = By.id("dropdownKebabRight9");
		public static final By KEBAB_OPEN = By.cssSelector("div.dropdown.dropdown-kebab-pf.pull-right.open");
		public static final By KEBAB_UNOPEN = By.cssSelector("div.dropdown.dropdown-kebab-pf.pull-right");
		public static final By MODAL_DIALOG = By.className("modal-dialog");
		public static final By ALL_BUTTONS = By.className("btn");
	}

	@Override
	public SelenideElement getRootElement() {
		return $(Element.ROOT).shouldBe(visible);
	}

	@Override
	public boolean validate() {
		return $(Element.ROOT).is(visible);
	}

	public Integer countConnections() {
		ElementsCollection allConnections =
				$(Element.ROOT).findAll(Element.CONNECTION).shouldBe(sizeGreaterThan(0));
		log.info("found {} connections", allConnections.size());
		return allConnections.size();
	}

	public SelenideElement getConnectionByTitle(String title) {
		log.info("searching for connection {}", title);
		return getElementContainingText(Element.CONNECTION, title);
	}

	public void goToConnection(String title) {
		log.info("searching for connection {}", title);
		this.getConnectionByTitle(title).shouldBe(visible).click();
	}

	public void deleteConnection(String title) {
		log.info("searching delete link for connection {}", title);
		this.getConnectionByTitle(title).parent().find(Element.KEBAB).shouldBe(visible).click();
		$(Element.ROOT).find(Button.DELETE).shouldBe(visible).click();
		log.info("looking for ok button");
		$(Element.MODAL_DIALOG).shouldBe(visible).findAll(Element.ALL_BUTTONS).shouldBe(sizeGreaterThanOrEqual(1))
				.filter(exactText("OK")).shouldBe(sizeGreaterThanOrEqual(1)).get(0).click();
	}

	public ElementsCollection getAllKebabElements(Boolean isOpen) {
		By kebabElm = isOpen ? Element.KEBAB_OPEN : Element.KEBAB_UNOPEN;
		return $(Element.ROOT).findAll(kebabElm);
	}

	public ElementsCollection getAllKebabButtons() {
		return $(Element.ROOT).findAll(Element.KEBAB);
	}

	public void clickOnAllKebabButtons() {
		ElementsCollection kebabButtons = this.getAllKebabButtons().shouldBe(sizeGreaterThan(0));
		for (SelenideElement kebabButton : kebabButtons) {
			log.debug("clicking on kebab button {}", kebabButton.getText());
			kebabButton.click();
		}
	}

	// Checking whether all open kebab elements are open-type (visible) and have proper menu actions
	public void checkAllKebabElementsAreDisplayed(Boolean shoulBeOpen, List<String> properActions) {
		ElementsCollection kebabElements = this.getAllKebabElements(shoulBeOpen).shouldBe(sizeGreaterThan(0));
		for (SelenideElement kebabElement : kebabElements) {
			kebabElement.shouldBe(visible);
			log.info("checking kebab menu");
			for (String pa : properActions) {
				log.info("testing action:searching for connection {}", pa);
				kebabElement.find(By.linkText(pa)).shouldBe(visible);
			}
		}
	}

	/**
	 *
	 * @param name name of connection
	 * @return all kebab menu buttons of one connection with name @param name
	 */
	public ElementsCollection getKebabMenuButtons(String name) {
		SelenideElement conObj = this.getConnectionByTitle(name).shouldBe(visible);
		conObj.parent().find(By.id("dropdownKebabRight9")).shouldBe(visible).click();
		return this.getConnectionByTitle(name).parent().findAll(By.className("dropdown-item")).shouldBe(sizeGreaterThanOrEqual(1));
	}

	public void clickKebabMenuButton(String button, String connectionName) {
		this.getKebabMenuButtons(connectionName).filter(exactText(button)).shouldBe(size(1)).get(0).click();
	}
}
