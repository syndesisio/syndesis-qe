/**
 * Created by jludvice on 8.3.17.
 */
import { CallbackStepDefinition, TableDefinition } from 'cucumber';
import { browser, ExpectedConditions } from 'protractor';
import { binding, given, then, when } from 'cucumber-tsflow';
import { Promise as P } from 'es6-promise';
import { expect, World } from './world';
import { Utils } from './utils';
import { User, UserDetails } from './common';
import { log } from '../../src/app/logging';
import { DashboardPage } from '../dashboard/dashboard.po';
import { IntegrationsListComponent } from '../integrations/list/list.po';


/**
 * Generic steps that can be used in various features
 * They may change state through world class.
 * See https://github.com/timjroberts/cucumber-js-tsflow#sharing-data-between-bindings
 */
@binding([World])
class CommonSteps {

  constructor(protected world: World) {
  }

  @when(/^"(\w+)" logs into the Syndesis.*$/i)
  public login(alias: string): P<any> {
    return this.world.app.login(this.world.user);
  }

  @given(/^clean application state$/)
  public async resetState(): P<any> {
    // user must be logged in (we need his token)
    const result = await this.world.app.login(this.world.user);
    // reset state or fail this step
    return this.world.app.resetState();
  }

  @given(/^application state "([^"]*)"$/)
  public async setState(jsonName: string): P<any> {
    // user must be logged in (we need his token)
    const result = await this.world.app.login(this.world.user);
    // reset state or fail this step
    return this.world.setState(jsonName);
  }

  /**
   * This method uses async/await and returns promise once it's done
   * @param linkTitle
   * @returns {Promise<P<any>>}
   */
  @when(/^"(\w+)" navigates? to the "([^"]*)" page.*$/)
  public async goToNavLink(alias: string, linkTitle: string): P<any> {
    // const link = await this.app.link(linkTitle);
    log.info(`navigating ${alias} to ${linkTitle} page`);
    if (linkTitle === 'Home') {
      return this.world.app.goHome();
    }
    const link = await this.world.app.link(linkTitle);
    expect(link, `Navigation link ${linkTitle} should exist`).to.exist;
    return link.element.click();
  }

  @then(/^(\w+)? ?is presented with the Syndesis page "([^"]*)"$/)
  public async verifyHomepage(alias: string, pageTitle: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const currentLink = await this.world.app.link(pageTitle);
    log.info(`${alias} is on current navlink: ${currentLink}`);
    expect(currentLink.active, `${pageTitle} link must be active`).to.be.true;
  }

  @then(/^(\w+)? ?is presented with the "([^"]*)" link*$/)
  public async verifyLink(alias: string, linkTitle: string): P<any> {
    const currentLink = await this.world.app.getLink(linkTitle);

    await browser.wait(ExpectedConditions.visibilityOf(currentLink), 5000, 'Link is not present.');
    return expect(currentLink.isPresent(), `There must be present a link ${linkTitle}`)
      .to.eventually.be.true;
  }

  @when(/clicks? on the "([^"]*)" button.*$/)
  public clickOnButton(buttonTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickButton(buttonTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));
  }

  @when(/clicks? on the "([^"]*)" link.*$/)
  public clickOnLink(linkTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickLink(linkTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));
  }

  @when(/clicks? on the random "([^"]*)" link.*$/)
  public clickOnLinkRandom(linkTitle: string, callback: CallbackStepDefinition): void {
    this.world.app.clickLinkRandom(linkTitle)
      .then(() => callback())
      // it may fail but we still want to let tests continue
      .catch((e) => callback(e));
  }

  @then(/^she is presented with the "([^"]*)" button.*$/)
  public expectButtonPresent(buttonTitle: string, callback: CallbackStepDefinition): void {
    const button = this.world.app.getButton(buttonTitle);

    expect(button.isPresent(), `There must be enabled button ${buttonTitle}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^she is presented with the "([^"]*)" tables*$/)
  public async expectTableTitlesPresent(tableTitles: string): P<any> {

    const tableTitlesArray = tableTitles.split(', ');

    for (const tableTitle of tableTitlesArray) {
      const table = this.world.app.getTitleByText(tableTitle);
      try {
        await browser.wait(ExpectedConditions.visibilityOf(table), 5000, 'Table is not present.');
        expect(table.isPresent(), `There must be present a table ${tableTitle}`).to.eventually.be.true;
      } catch (e) {
        return P.reject(e);
      }
    }

    return P.resolve();
  }

  @then(/^she is presented with the "([^"]*)" elements*$/)
  public expectElementsPresent(elementClassNames: string, callback: CallbackStepDefinition): void {

    const elementClassNamesArray = elementClassNames.split(',');

    for (const elementClassName of elementClassNamesArray) {
      this.expectElementPresent(elementClassName, callback);
    }
  }

  //unused
  @then(/^she is presented with the "([^"]*)"$/)
  public expectElementPresent(elementClassName: string, callback: CallbackStepDefinition): void {

    const element = this.world.app.getElementByClassName(elementClassName);
    expect(element.isPresent(), `There must be present a element ${elementClassName}`)
      .to.eventually.be.true;

    expect(element.isPresent(), `There must be enabled element ${elementClassName}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Integration "([^"]*)" is present in top 5 integrations$/)
  public expectIntegrationPresentinTopFive(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying integration ${name} is present in top 5 integrations`);
    const page = new DashboardPage();
    expect(page.isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can see "([^"]*)" connection on dashboard page$/)
  public expectConnectionTitlePresent(connectionName: string, callback: CallbackStepDefinition): void {
    const dashboard = new DashboardPage();
    const connection = dashboard.getConnection(connectionName);
    expect(connection.isPresent(), `There should be present connection ${connectionName}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can not see "([^"]*)" connection on dashboard page anymore$/)
  public expectConnectionTitleNonPresent(connectionName: string, callback: CallbackStepDefinition): void {
    const dashboard = new DashboardPage();
    const connection = dashboard.getConnection(connectionName);
    expect(connection.isPresent(), `There shouldnt be a present connection ${connectionName}`)
      .to.eventually.be.false.notify(callback);
  }

  @when(/^Camilla deletes the "([^"]*)" integration in top 5 integrations$/)
  public deleteIntegrationOnDashboard(integrationName: string): P<any> {
    log.info(`Trying to delete ${integrationName} on top 5 integrations table`);
    const listComponent = new IntegrationsListComponent();
    return listComponent.clickDeleteIntegration(integrationName);
  }

  @then(/^Camilla can not see "([^"]*)" integration in top 5 integrations anymore$/)
  public expectIntegrationPresentOnDashboard(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying if integration ${name} is present`);
    const dashboard = new DashboardPage();
    expect(dashboard.isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.false.notify(callback);
  }

  @then(/^she can see success notification$/)
  public successNotificationIsPresent(): P<any> {
    const allertSucces = this.world.app.getElementByClassName('alert-success');
    return browser.wait(ExpectedConditions.visibilityOf(allertSucces), 6000, 'OK button not loaded in time');
  }

  @then(/^she can see "([^"]*)" in alert\-success notification$/)
  async successNotificationIsPresentWithError(textMessage: string): P<any> {
    try {
      const allertSucces = this.world.app.getElementByClassName('alert-success');      
      await browser.wait(ExpectedConditions.visibilityOf(allertSucces), 6000, 'alert-success element not found');
      await expect(allertSucces.getText(), 'Text message was different!').to.eventually.be.equal(textMessage);      

    } catch (error) {
      return P.reject(error);
    }

    log.info(`Text message "${textMessage}" was found.`);
    return P.resolve();
  }

  @when(/^she selects "([^"]*)" from "([^"]*)" dropdown$/)
  public async selectFromDropDownById(option: string, selectId: string): P<any> {
    const selectSelector = `select[id="${selectId}"]`;
    const selectElement = await this.world.app.getElementByCssSelector(selectSelector);
    await browser.wait(ExpectedConditions.visibilityOf(selectElement), 30 * 1000, `Input ${selectId} not loaded in time`);
    return this.world.app.selectOption(selectElement, option);
  }

  /**
   * Scroll the webpage.
   *
   * @param topBottom possible values: top, bottom
   * @param leftRight possible values: left, right
   * @returns {Promise<any>}
   */
  @when(/^scroll "([^"]*)" "([^"]*)"$/)
  public async scroll(topBottom: string, leftRight: string): P<any> {
    // get real width and height
    const width = await browser.executeScript(() => $(document).width());
    const height = await browser.executeScript(() => $(document).height());

    const directions: Object = {
      top: 0,
      bottom: height,
      left: 0,
      right: width,
    };

    if (!directions.hasOwnProperty(topBottom) || !directions.hasOwnProperty(leftRight)) {
      return P.reject(`unknown directions [${topBottom}, ${leftRight}`);
    }
    const x = directions[leftRight];
    const y = directions[topBottom];

    log.info(`scrolling to [x=${x},y=${y}]`);
    return browser.driver.executeScript((browserX, browserY) => window.scrollTo(browserX, browserY), x, y);
  }

  @then(/^she stays there for "(\d+)" ms$/)
  public async sleep(ms: number): P<any> {
    await Utils.delay(ms);
  }

}

export = CommonSteps;
