import * as webdriver from 'selenium-webdriver';
import { Promise as P } from 'es6-promise';
import { element, by, ElementFinder } from 'protractor';
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
}

export class ConnectionsListPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-connections-list-page'));
  }

  listComponent(): ConnectionsListComponent {
    return new ConnectionsListComponent();
  }
}
