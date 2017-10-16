import {binding, then, when} from 'cucumber-tsflow';
import {World, P, expect} from '../common/world';
import {OAuthSettingsComponent, SettingsPage} from './settings.po';

@binding([World])
class SettingsSteps {
  constructor(protected world: World) {
  }

  @then(/^"([^"]*)" is presented with "([^"]*)" settings tab$/)
  public activeTab(user: string, tabName: string): P<any> {

    const page = new SettingsPage();
    const activeTab = page.activeTab();

    return expect(activeTab)
      .to.eventually.be.equal(tabName, `Default settings tab should be ${tabName}`);
  }

  @then(/^settings item "([^"]*)" has button "([^"]*)"$/)
  public settingsItemHasButton(itemTitle: string, buttonTitle: string): P<any> {
    const page = new SettingsPage();

    return page.getSettingsItem(itemTitle);
  }

  @when(/^"([^"]*)" clicks to the "([^"]*)" item "([^"]*)" button$/)
  public clickSettingsButton(userAlias: string, itemTitle: string, buttonTitle: string): P<any> {
    const page = new SettingsPage();
    return page.clickButton(itemTitle, buttonTitle);
  }

  @when(/^fill form in "([^"]*)" settings item$/)
  public fillSettingsItemForm(itemTitle: string): P<any> {
    const settings = new OAuthSettingsComponent();
    const toFill = this.world.testConfig.settings[itemTitle];
    return settings.fillSettingsItemForm(itemTitle, toFill);
  }

  @then(/^settings item "([^"]*)" must have alert with text "([^"]*)"$/)
  public assertSettingsAlertText(itemTitle: string, alertText: string): P<any> {
    const settings = new OAuthSettingsComponent();
    return expect(settings.getAlertText(itemTitle))
      .to.eventually.contain(alertText);
  }
}


export = SettingsSteps;
