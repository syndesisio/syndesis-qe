import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder, ElementArrayFinder } from 'protractor';
import { SyndesisComponent } from '../../common/common';
import { log } from '../../../src/app/logging';

export class ConnectionsListComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-connections-list'));
  }

  async countConnections(): P<number> {
    const found = await this.rootElement().all(by.css('h2.card-pf-title.text-center')).getWebElements();
    log.info(`found ${found.length} connections`);
    return found.length;
  }

  getConnection(connectionTitle: string): ElementFinder {
    log.info(`searching for connection ${connectionTitle}`);
    return this.rootElement().$(`div.connection[title="${connectionTitle}"]`);
  }

  goToConnection(connectionTitle: string): P<any> {
    log.info(`searching for connection ${connectionTitle}`);
    return this.getConnection(connectionTitle).getWebElement().click();
  }

  deleteConnection(connectionTitle: string): P<any> {
    log.info(`searching for delete link for connection ${connectionTitle}`);
    this.getConnection(connectionTitle).element(by.id('dropdownKebabRight9')).click();
    this.rootElement().element(by.linkText('Delete')).click();
    return this.rootElement().element(by.buttonText('Delete')).click();
  }

  getAllKebabElements(isOpen: boolean): ElementArrayFinder {
    const open = isOpen ? ".open" : "";
    return this.rootElement().all(by.css(`div.dropdown.dropdown-kebab-pf.pull-right${open}`));
  }

  getAllKebabButtons(): ElementArrayFinder {
    return this.rootElement().all(by.css('button.btn.btn-link.dropdown-toggle'));
  }

  async clickOnAllKebabButtons(): P<any> {
    const kebabButtons = await this.getAllKebabButtons();
    if (kebabButtons.length == 0) {
      throw new Error(`There should be some prepared connections!`);
    }
    const promises: P<any>[] = [];
    kebabButtons.forEach(kebabB => {
      log.debug(`clicking on kebab button`);
      promises.push(kebabB.getWebElement().click());
    });
    return P.all(promises);
  }

  // Checking whether all open kebab elements are open-type (visible) and have proper menu actions
  async checkAllKebabElementsAreDisplayed(shoulBeOpen: boolean, properActions: string[]): P<any> {
    const promises: P<any>[] = [];
    const kebabElements = await this.getAllKebabElements(shoulBeOpen);
    if (kebabElements.length == 0) {
      throw new Error(`There should be some prepared connections!`);
    }
    kebabElements.forEach(kebabE => {
      log.debug(`checking kebab menu`);
      promises.push(kebabE.getWebElement().isDisplayed().catch((e) => P.reject(e)));
      properActions.forEach(pa => {
        log.info(`testing action :searching for connection ${pa}`);
        promises.push(kebabE.element(by.cssContainingText('a', pa)).getWebElement().isDisplayed().catch((e) => P.reject(e)));
      });
    });
    return P.all(promises).catch((e) => P.reject(e));
  }

}

export class ConnectionsListPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-connections-list-page'));
  }

  listComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
  }
}
