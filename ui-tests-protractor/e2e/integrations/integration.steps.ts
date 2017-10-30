/**
 * Created by jludvice on 8.3.17.
 */
import { Utils } from '../common/utils';
import { binding, then, when } from 'cucumber-tsflow';
import { CallbackStepDefinition } from 'cucumber';
import { expect, P, World } from '../common/world';
import { IntegrationAddStepPage, IntegrationEditPage, StepFactory, FlowViewComponent } from '../integrations/edit/edit.po';
import { ActionConfigureComponent, TwitterSearchActionConfigureComponent } from '../integrations/edit/edit.po';
import { ListActionsComponent, IntegrationConfigureBasicFilterStepPage, ConnectionSelectComponent } from '../integrations/edit/edit.po';
import { log } from '../../src/app/logging';
import { IntegrationsListComponent, IntegrationsListPage } from '../integrations/list/list.po';
import { IntegrationDetailPage, IntegrationDetailPageFactory } from './detail/detail.po';

import { element, by, ElementFinder, browser, ExpectedConditions } from 'protractor';

@binding([World])
class IntegrationSteps {

  constructor(protected world: World) {
  }

  @when(/defines integration name "([^"]*)"$/)
  public defineIntegrationName(integrationName: string): P<any> {
    const page = new IntegrationEditPage();
    return page.basicsComponent().setName(integrationName);
  }

  @then(/^she is presented with a visual integration editor$/)
  public async editorOpened(): P<any> {
    const editPage = new IntegrationEditPage();
    const selectConnectionPage = new ConnectionSelectComponent();
    const flowViewComponent = new FlowViewComponent();

    try {
      await expect(editPage.rootElement().isPresent(), 'there must be edit page root element')
        .to.eventually.be.true;
      await expect(selectConnectionPage.rootElement().isPresent(), 'there must be select connection page page root element')
        .to.eventually.be.true;
      await expect(flowViewComponent.rootElement().isPresent(), 'there must be flow view component root element')
        .to.eventually.be.true;
    } catch (e) {
      P.reject(e);
    }

    return P.resolve();
  }

  @then(/^she is presented with a visual integration editor for "([^"]*)"$/)
  public editorOpenedFor(integrationName: string): P<any> {
    return this.editorOpened().then(() => {
      // ensure we're on editor page and then check integration name
      const page = new IntegrationEditPage();
      return expect(page.flowViewComponent().getIntegrationName(), `editor must display integration name ${integrationName}`)
        .to.eventually.be.equal(integrationName);
    }).catch(e => P.reject(e));
  }

  @then(/^Camilla is presented with "([^"]*)" integration details$/)
  public verifyIntegrationDetails(integrationName: string): P<any> {
    const detailPage = new IntegrationDetailPage();
    return expect(detailPage.getIntegrationName(), `Integration detail page must show integration name`)
      .to.eventually.be.equal(integrationName);
    // todo add more assertion on integration details page
  }

  @when(/^Camilla selects the "([^"]*)" integration.*$/)
  public selectConnection(itegrationName: string): P<any> {
    const page = new IntegrationsListPage();
    return page.listComponent().goToIntegration(itegrationName);
  }

  @when(/^she selects "([^"]*)" integration action$/)
  public selectIntegrationAction(action: string): P<any> {
    const page = new ListActionsComponent();
    if (action === 'Create Opportunity') {
      log.warn(`Action ${action} is not available`);
      return page.selectAction('Create Salesforce object');
    }
    return page.selectAction(action);
  }

  @when(/^Camilla deletes the "([^"]*)" integration*$/)
  public deleteIntegration(integrationName: string): P<any> {
    const listComponent = new IntegrationsListComponent();
    return listComponent.clickDeleteIntegration(integrationName);
  }

  @when(/^Camilla deletes the integration on detail page*$/)
  public async deleteIntegrationOnDetailPage(): P<any> {
    const detailPage = new IntegrationDetailPage();
    return detailPage.deleteIntegration();
  }

  @then(/^she can see on detail page that integration is "([^"]*)" status$/)
  public async checkStatus(expectedStatus: string): P<any> {
    const detailPage = new IntegrationDetailPage();
    const status = await detailPage.getStatus();
    log.info(`Status: ${status}`);
    return expect(detailPage.getStatus(), `Status on integration detail page should be equal to expected status`)
      .to.eventually.be.equal(expectedStatus);
  }

  @then(/^she clicks on integration in "([^"]*)" status and check on detail if status match and appropriate actions are available$/)
  public async clickOnIntegrationInStatus(status: string): P<any> {
    const listComponent = new IntegrationsListComponent();
    const integrationByStatus = listComponent.getIntegrationByStatus(status);

    const isPresent = await integrationByStatus.isPresent();
    if (!isPresent) {
      log.warn(`No integration for status ${status} is present in integration list.`);
      return P.resolve();
    }

    integrationByStatus.click();

    const integrationDetailPageFactory = new IntegrationDetailPageFactory();
    const detailPage = integrationDetailPageFactory.getDetailPage(status);

    for (const action of detailPage.actionsSet) {
      log.info(`Action: ${action}`);
      const actionButton = detailPage.getActionButton(action);

      try {
        await expect(actionButton.isPresent(), `There should by button for ${action} action on ${status} status`).to.eventually.be.true;
      } catch (e) {
        log.info(`Error catched: ${e}`);
        return P.reject(e);
      }
    }

    try {
      await expect(detailPage.getStatus(), `Status on detail page should be equal to expected status`).to.eventually.be.equal(status);
    } catch (e) {
      log.info(`Error catched: ${e}`);
      return P.reject(e);
    }

    return detailPage.getActionButton('Done').click();
  }

  @then(/^she go trough whole list of integrations and check on detail if status match and appropriate actions are available$/)
  public async goTrouhListAndCheckDetails(): P<any> {
    const listComponent = new IntegrationsListComponent();
    const integrations = await listComponent.getAllIntegrations();

    for (let i = 0; i < integrations.length - 1; i++) {
      const integration = await listComponent.getAllIntegrations().get(i);

      const status = await listComponent.getIntegrationItemStatus(integration);
      log.info(`Status: ${status}`);

      integration.click();

      const integrationDetailPageFactory = new IntegrationDetailPageFactory();
      const detailPage = integrationDetailPageFactory.getDetailPage(status);

      for (const action of detailPage.actionsSet) {
        log.info(`Action: ${action}`);
        const actionButton = detailPage.getActionButton(action);

        try {
          await expect(actionButton.isPresent(), `There should by button for ${action} action on ${status} status`).to.eventually.be.true;
        } catch (e) {
          log.info(`Error catched: ${e}`);
          return P.reject(e);
        }
      }

      try {
        await expect(detailPage.getStatus(), `Status on detail page should be equal to expected status`).to.eventually.be.equal(status);
      } catch (e) {
        log.info(`Error catched: ${e}`);
        return P.reject(e);
      }

      await detailPage.done();
    }

    return P.resolve();
  }

  @when(/^she selects "([^"]*)" integration step$/)
  public addStep(stepName: string): P<any> {
    log.info(`Adding ${stepName} step to integration`);
    const page = new IntegrationAddStepPage();
    return page.addStep(stepName);
  }

  @then(/^Integration "([^"]*)" is present in integrations list$/)
  public expectIntegrationPresent(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying integration ${name} is present`);
    const page = new IntegrationsListPage();
    expect(page.listComponent().isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can not see "([^"]*)" integration anymore$/)
  public expectIntegrationNotPresent(name: string, callback: CallbackStepDefinition): void {
    log.info(`Verifying if integration ${name} is present`);
    const page = new IntegrationsListPage();
    expect(page.listComponent().isIntegrationPresent(name), `Integration ${name} must be present`)
      .to.eventually.be.false.notify(callback);
  }

  @then(/^she wait until integration "([^"]*)" get into "([^"]*)" state$/)
  public async waitForIntegrationState(integrationName: string, integrationState: string): P<any> {
    const page = new IntegrationsListPage();

    const integration = await page.listComponent().getIntegration(integrationName);
    const integrationActiveState = integration.element(by.cssContainingText('syndesis-integration-status', integrationState));

    await browser.wait(ExpectedConditions.visibilityOf(integrationActiveState), 300000, `Integration didnt get into ${integrationState}.`);

    return P.resolve();
  }


  @then(/^she is presented with a add step page$/)
  public addStepPageOpened(callback: CallbackStepDefinition): void {
    const page = new IntegrationAddStepPage();
    expect(page.rootElement().isPresent(), 'there must be add step page root element')
      .to.eventually.be.true.notify(callback);
  }

  @then(/^she is presented with a "([^"]*)" step configure page$/)
  public async configureStepPageOpen(stepType: string): P<any> {
    const stepFactory = new StepFactory();
    const page = stepFactory.getStep(stepType, '');
    try {
      await expect(page.rootElement().isPresent(), 'there must be add step page root element')
        .to.eventually.be.true;
      await page.validate();
    } catch (e) {
      return P.reject(e);
    }

    return P.resolve();
  }

  @then(/^she fill configure page for "([^"]*)" step with "([^"]*)" parameter$/)
  public fillStepConfiguration(stepType: string, parameter: string): P<any> {
    const stepFactory = new StepFactory();
    const page = stepFactory.getStep(stepType, parameter);
    return page.fillConfiguration();
  }

  @then(/^she adds "([^"]*)" random steps and then check the structure$/)
  public async addRandomStepsAndCheckRest(numberOfSteps: number): P<any> {
    log.info(`Adding random steps`);
    const flowViewComponent = new FlowViewComponent();

    const array = await flowViewComponent.getStepsArray();

    this.world.app.clickButton('Add a Step');

    const links = this.world.app.getLinks('Add a step');
    const count = await links.count();

    const randomIndexes = [];
    for (let i = 0; i < numberOfSteps; i++) {
      randomIndexes.push(Math.floor((Math.random() * count)));
    }

    const page = new IntegrationAddStepPage();

    for (const randomIndex of randomIndexes) {
      links.get(randomIndex).click();

      const stepType = 'Basic Filter';
      const stepParameter = 'ANY of the following, pathx' + randomIndex + ', Contains, valuex' + randomIndex;

      page.addStep(stepType);

      const stepFactory = new StepFactory();
      const stepPage = stepFactory.getStep(stepType, stepParameter);

      await stepPage.fillConfiguration();

      this.world.app.clickButton('Next');
      this.world.app.clickButton('Add a Step');

      array.splice(randomIndex, 0, stepParameter);
    }

    const array2 = await flowViewComponent.getStepsArray();

    for (let i = 0; i < array2.length; i++) {
      log.info(`assserting "${array[i]}" and "${array2[i]}"`);
      try {
        expect(array[i]).to.be.equal(array2[i]);
      } catch (e) {
        return P.reject(e);
      }
    }

    return P.resolve();
  }

  @then(/^she delete "([^"]*)" random steps and check rest$/)
  public async deleteRandomStepsAndCheckRest(numberOfSteps: number): P<any> {
    log.info(`Deleting random steps`);
    const flowViewComponent = new FlowViewComponent();

    const array = await flowViewComponent.getStepsArray();
    const trashes = this.world.app.getElementsByClassName('delete-icon');
    const count = await trashes.count();

    const randomIndexes = [];
    for (let i = 0; i < numberOfSteps; i++) {
      randomIndexes.push(Math.floor(Math.random() * (count - 2 - i)));
    }

    for (const randomIndex of randomIndexes) {
      trashes.get(randomIndex + 1).click();
      try {
        await this.world.app.clickOnFirstVisibleButton('OK');
      } catch (e) {
        return P.reject(e);
      }
      array.splice(randomIndex, 1);
    }

    const array2 = await flowViewComponent.getStepsArray();

    for (let i = 0; i < array.length; i++) {
      log.info(`assserting "${array[i]}" and "${array2[i]}"`);
      try {
        expect(array[i]).to.be.equal(array2[i]);
      } catch (e) {
        return P.reject(e);
      }
    }

    return P.resolve();
  }

  @then(/^she delete step on position "([^"]*)" and check rest$/)
  public async deleteStepOnPositionAndCheckRest(positionOfStep: number): P<any> {
    log.info(`Deleting step on position "${positionOfStep}"`);
    const flowViewComponent = new FlowViewComponent();

    const array = await flowViewComponent.getStepsArray();
    const trashes = this.world.app.getElementsByClassName('delete-icon');

    const indexOfStep: number = Number(positionOfStep) + 1;
    trashes.get(indexOfStep).click();
    try {
      await this.world.app.clickOnFirstVisibleButton('OK');
    } catch (e) {
      return P.reject(e);
    }
    array.splice(positionOfStep, 1);

    const array2 = await flowViewComponent.getStepsArray();

    for (let i = 0; i < array.length; i++) {
      log.info(`assserting "${array[i]}" and "${array2[i]}"`);
      try {
        expect(array[i]).to.be.equal(array2[i]);
      } catch (e) {
        return P.reject(e);
      }
    }

    return P.resolve();
  }

  @then(/^she is presented with an actions list$/)
  public expectActionListIsPresent(): void {
    const page = new ListActionsComponent();
    browser.wait(ExpectedConditions.visibilityOf(page.rootElement()), 5000, 'Actions List not loaded');
    expect(page.rootElement().isDisplayed(), 'There must be action list loaded')
      .to.eventually.be.true;
  }

  @when(/clicks? on the integration save button.*$/)
  public async clickOnSaveButton(): P<any> {
    let saveButton = await this.world.app.getButton('Save');
    const isSaveButtonPresent = await saveButton.isPresent();

    if (!isSaveButtonPresent) {
      log.warn(`Save button is not present on integration edit.`);
      saveButton = await this.world.app.getButton('Save as Draft');
    }

    return saveButton.click();
  }

  @then(/^add new basic filter rule with "([^"]*)" parameters$/)
  public async addBasicFilterRule(rule: string): P<any> {
    const basicFilterStepPage = new IntegrationConfigureBasicFilterStepPage('');
    await basicFilterStepPage.initialize();
    return basicFilterStepPage.addRule(rule);
  }

  @then(/^delete "([^"]*)" random basic filter rule$/)
  public deleteRandomFilterRules(numberOfRules: number): P<any>[] {
    let promiseArray: P<any>[];
    promiseArray = [];

    for (let i = 0; i < numberOfRules; i++) {
      promiseArray.push(this.world.app.clickElementRandom('fa-trash-o'));
    }

    return promiseArray;
  }

  @then(/^delete basic filter rule on position "([^"]*)"$/)
  public deleteFilterRuleOnPosition(position: number): P<any> {
    const trashes = this.world.app.getElementsByClassName('fa-trash-o');
    return trashes.get(position - 1).click();
  }

  //Kebab menu test, #553 -> part #548, #549.
  @when(/^clicks on the kebab menu icon of each available Integration and checks whether menu is visible and has appropriate actions$/)
  public clickOnAllKebabMenus(): P<any> {
    const integrationsListComponent = new IntegrationsListComponent();
    return integrationsListComponent.checkAllIntegrationsKebabButtons();
  }

  // Twitter search specification
  @then(/^she fills keywords field with random text to configure search action$/)
  public fillKeywords(): P<any> {
    const actionConfComponent = new TwitterSearchActionConfigureComponent();
    const value = Utils.randomString(20, 'abcdefghijklmnopqrstuvwxyz');
    return actionConfComponent.fillKeywordsValue(value);
  }

  @then(/^she fills "([^"]*)" action configure component input with "([^"]*)" value$/)
  public async fillActionConfigureField(fieldId: string, value: string): P<any> {
    const actionConfComponent = new IntegrationEditPage().actionConfigureComponent();
    await browser.wait(ExpectedConditions.visibilityOf(actionConfComponent.getInputById(fieldId)), 5000, 'Input is not visible');
    return actionConfComponent.fillInputById(fieldId, value);
  }

  /**
   * whether it's start or finish connection
   * @param type
   * @param callback
   */
  @then(/^she is prompted to select a "([^"]*)" connection from a list of available connections$/)
  public async verifyTypeOfConnection(type: string): P<any> {
    const page = new IntegrationEditPage();
    const connection = await page.flowViewComponent().flowConnection(type);
    const isActive: boolean = await connection.isActive();

    if (!isActive) {
      log.warn(`Connection is not active! Refreshingk"`);
      await browser.refresh();
    }

    return expect(connection.isActive(), `${type} connection must be active`)
      .to.eventually.be.true;
  }

  /**
   * check you are on the correct page
   * @param text
   */
  @then(/^she is presented with "([^"]*)" (page|editor)$/)
  public verifyPageByText(text: string, callback: CallbackStepDefinition): void {
    // 1. :
    const editPage = new IntegrationEditPage();
    let elementName;
    let element: ElementFinder;

    switch (text) {
      case 'Choose a Finish Connection': {
        elementName = 'h1';
        element = editPage.rootElement().element(by.cssContainingText(elementName, text));
        expect(element.isPresent(), `there must be edit page root element`).to.eventually.be.true;
        break;
      }
      case 'Add to Integration': {
        elementName = 'h1';
        element = editPage.rootElement().element(by.cssContainingText(elementName, text));
        expect(element.isPresent(), `there must be edit page root element`).to.eventually.be.true;
        break; 
      }
      default: {
        P.reject(`Page ${text} doesn't exist`);
      }
    }
  }
}

export = IntegrationSteps;
