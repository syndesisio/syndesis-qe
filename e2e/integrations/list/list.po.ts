import { SyndesisComponent } from '../../common/common';
import { $, browser, ExpectedConditions, by, ElementFinder, ElementArrayFinder, element } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';
import { IntegrationsUtils } from '../utils/integrations.utils';

import * as webdriver from 'selenium-webdriver';


export class IntegrationsListComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-integrations-list');
  }

  private integrationEntry(name: string): ElementFinder {
    return this.rootElement().element(by.cssContainingText('.list-pf-title', name));
  }

  async isIntegrationPresent(name: string): P<boolean> {
    log.info(`Checking if integration ${name} is present in the list`);
    const integration = this.integrationEntry(name);
    return integration.isPresent();
  }

  goToIntegration(integrationName: string): P<any> {
    return this.integrationEntry(integrationName).getWebElement().click();
  }

  editIntegration(name: string): P<any> {
    return this.integrationEntry(name).click();
  }

  async clickDeleteIntegration(integrationName: string): P<any> {
    log.info(`clicking delete link for integration ${integrationName}`);

    const parentElement = this.rootElement().element(by.className('list-pf-item'));

    try {
      await browser.wait(ExpectedConditions.visibilityOf(parentElement), 6000, 'No integration present');

      const parentElements = this.getAllIntegrations().filter((async(elem, index) => {
        const text = await this.getIntegrationName(elem);
        return text === integrationName;
      }).bind(this));

      parentElements.first().element(by.className('dropdown-kebab-pf')).click();
      this.rootElement().element(by.linkText('Delete')).click();

      const okButton = element(by.buttonText('OK'));
      browser.wait(ExpectedConditions.visibilityOf(okButton), 30000, 'OK button not loaded in time');

      return okButton.click();
    } catch (e) {
      return P.reject(e);
    }
  }

  getAllIntegrations(): ElementArrayFinder {
    return this.rootElement().all(by.className(`list-pf-item`));
  }

  async getIntegrationName(integration: ElementFinder): P<string> {
    let name: string;
    const isNamePresent = await integration.element(by.className('name')).isPresent();

    if (isNamePresent) {
      name = await integration.element(by.className('name')).getText();
    } else {
      log.warn('Name is not present!');
      name = await integration.element(by.className('description')).getText();
    }

    return name;
  }

  //kebab
  async getIntegrationItemStatus(item: ElementFinder): P<string> {
    return item.element(by.css('syndesis-integration-status')).getText();
  }
  //kebab
  getKebabButtonFromItem(item: ElementFinder): ElementFinder {
    return item.element(by.css('button.btn.btn-link'));
  }
  //kebab
  getKebabElement(isOpen: boolean, item: ElementFinder): ElementFinder {
    const open = isOpen ? '.open' : '';
    return item.element(by.css(`div.dropdown.dropdown-kebab-pf.pull-right${open}`));
  }

  async isKebabButtonHidden(item: ElementFinder): P<boolean> {
    return await this.getKebabButtonFromItem(item).getAttribute('visibility') === 'hidden';
  }


  async checkIfKebabHasWhatShouldHave(item: ElementFinder, status: string): P<any> {
    const promises: P<any>[] = [];
    //    const status: IntegrationStatus = await this.getIntegrationItemStatus(item);
    const properActions: string[] = IntegrationsUtils.getProperKebabActions(status);
    if (properActions.length === 0) {
      throw new Error(`Wrong status!`);
    }
    log.debug(`checking kebab menu of kebab element:`);
    const kebabE = await this.getKebabElement(true, item);
    promises.push(kebabE.getWebElement().isDisplayed().catch((e) => P.reject(e)));
    properActions.forEach(pa => {
      log.info(`testing action :searching for kebab action *${pa}*`);
      promises.push(kebabE.element(by.cssContainingText('a', pa)).getWebElement().isDisplayed().catch((e) => P.reject(e)));
    });
    return P.all(promises).catch((e) => P.reject(e));
  }

  async checkAllIntegrationsKebabButtons(): P<any> {
    const promises: P<any>[] = [];
    const integrationsItems = await this.getAllIntegrations();
    // have to reverse, since expanded kebab of integrations entry hides kebab of the below integration item.
    // and this is problem for selenium.
    integrationsItems.reverse();
    //2. go through elements and:
    //    integrationsTems.forEach(item => { // .forEach doesn't work for await
    for (const item of integrationsItems) {
      const name = await this.getIntegrationName(item);
      log.info(`INTEGRATION ITEM NAME: *${name}*`);
      //    check whether status is not "Deleted";
      const status = await this.getIntegrationItemStatus(item);
      log.info(`INTEGRATION ITEM STATUS: *${status}*`);
      if (status === 'Deleted') {

        //check whether it dont have any kebab
        if (!this.isKebabButtonHidden(item)) {
          promises.push(P.reject('Kebab button should be hidden'));
        }
      } else {
        log.info(`clicking on kebab button`);
        const kebabB = await this.getKebabButtonFromItem(item);
        promises.push(kebabB.getWebElement().click());
        log.info(`checking whether integration ${name} kebab has all actions that should have`);
        promises.push(this.checkIfKebabHasWhatShouldHave(item, status).catch((e) => P.reject(e)));
      }
    }
    return P.all(promises).catch((e) => P.reject(e));
  }
}

export class IntegrationsListPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-integrations-list-page');
  }

  listComponent(): IntegrationsListComponent {
    return new IntegrationsListComponent();
  }
}
