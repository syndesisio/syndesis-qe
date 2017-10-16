import {SyndesisComponent} from '../common/common';
import {$, by, ElementArrayFinder, ElementFinder} from 'protractor';
import {P} from '../common/world';
import {log} from '../../src/app/logging';

export class SettingsPage implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-settings-root');
  }

  /**
   * Get title of active settings tab
   * @returns {Promise<string>} title of active settings tab
   */
  async activeTab(): P<string> {
    const activeTab = this.rootElement().$('ul.nav-tabs li.active a');
    return activeTab.getText();
  }

  /**
   * Support method for finding settings items by name
   * @param {ElementFinder} parent search child elements of this element
   * @returns {ElementArrayFinder} found elements
   */
  listSettingsItems(parent: ElementFinder = this.rootElement()): ElementArrayFinder {
    return parent
      .$('pfng-list')
      .$$('div.list-pf-item');
  }

  /**
   * Fetch all settings items and find proper one according to given name.
   * @param {string} name name of settings item
   * @returns {Promise<ElementFinder | any>} return element or reject promise
   */
  async getSettingsItem(name: string): P<ElementFinder | any> {
    const items = await this.listSettingsItems();
    log.info(`searching for ${name} in ${items.length} items`);

    for (const item of items) {
      const title = await item.$('div.list-pf-title').getText();
      if (title === name) {
        return item;
      }
    }
    return P.reject(`item ${name} not found`);
  }

  /**
   * Click on button which is child element of given settings item
   * @param {string} settingsItemName name of settings item
   * @param {string} buttonTitle title of button
   * @returns {Promise<any>} resolved once clicked
   */
  async clickButton(settingsItemName: string, buttonTitle: string): P<any> {
    const item: ElementFinder = await this.getSettingsItem(settingsItemName);
    log.info(`click ${buttonTitle} on ${settingsItemName}`);
    return item.element(by.buttonText(buttonTitle)).click();
  }
}


/**
 * Represents oauth apps tab.
 */
export class OAuthSettingsComponent extends SettingsPage {
  rootElement(): ElementFinder {
    return super.rootElement().$('syndesis-oauth-apps');
  }

  /**
   *
   * @param {string} itemTitle title of settings entry
   * @param formData object where key is name of input field and value it's content
   * @returns {Promise<any>} resolved when all fields are finished
   */
  async fillSettingsItemForm(itemTitle: string, formData: any): P<any> {
    log.info(`filling ${itemTitle} with data '${JSON.stringify(formData)}'`);
    const item: ElementFinder = await this.getSettingsItem(itemTitle);
    const promises = [];
    for (const key of Object.keys(formData)) {
      const input = item.$(`input[name="${key}"]`);
      promises.push(input.sendKeys(formData[key]));
    }
    return P.all(promises);
  }

  /**
   * Alert should be within the settings item
   * @param {string} itemTitle title of settings item (like Twitter)
   * @returns {Promise<string>} string content of alert
   */
  async getAlertText(itemTitle: string): P<string> {
    const item: ElementFinder = await this.getSettingsItem(itemTitle);
    return item.$('div.alert').getText();
  }
}
