/**
 * Created by jludvice on 8.3.17.
 */
import { Utils } from '../common/utils';
import { binding, then, when } from 'cucumber-tsflow';
import { CallbackStepDefinition } from 'cucumber';
import { expect, P, World } from '../common/world';
import { IntegrationAddStepPage, IntegrationEditPage, StepFactory } from '../integrations/edit/edit.po';
import { ListActionsComponent, ActionConfigureComponent, IntegrationConfigureBasicFilterStepPage } from '../integrations/edit/edit.po';
import { log } from '../../src/app/logging';
import { IntegrationsListComponent, IntegrationsListPage } from '../integrations/list/list.po';

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
  public editorOpened(): P<any> {
    const page = new IntegrationEditPage();
    return expect(page.rootElement().isPresent(), 'there must be edit page root element')
      .to.eventually.be.true;
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

  @when(/^Camilla selects the "([^"]*)" integration.*$/)
  public selectConnection(itegrationName: string): P<any> {
    const page = new IntegrationsListPage();
    return page.listComponent().goToIntegration(itegrationName);
  }

  @when(/^she selects "([^"]*)" integration action$/)
  public selectIntegrationAction(action: string): P<any> {
    const page = new ListActionsComponent();
    return page.selectAction(action);
  }

  @when(/^Camilla deletes the "([^"]*)" integration*$/)
  public deleteIntegration(integrationName: string): P<any> {
    const listComponent = new IntegrationsListComponent();
    return listComponent.clickDeleteIntegration(integrationName);
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

  @then(/^she is presented with a add step page$/)
  public addStepPageOpened(callback: CallbackStepDefinition): void {
    const page = new IntegrationAddStepPage();
    expect(page.rootElement().isPresent(), 'there must be add step page root element')
      .to.eventually.be.true.notify(callback);
  }

  @then(/^she is presented with a "([^"]*)" step configure page$/)
  public configureStepPageOpen(stepType: string): void {
    const stepFactory = new StepFactory();
    const page = stepFactory.getStep(stepType, '');
    expect(page.rootElement().isPresent(), 'there must be add step page root element')
      .to.eventually.be.true;
    expect(page.validate(), 'page must contain certain elements')
      .to.eventually.be.true;
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

    const array = await this.getStepsArray();

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
      const stepParameter = 'ANY of the following, path' + randomIndex + ', not contains, value' + randomIndex;

      page.addStep(stepType);

      const stepFactory = new StepFactory();
      const stepPage = stepFactory.getStep(stepType, stepParameter);

      stepPage.fillConfiguration();

      this.world.app.clickButton('Next');
      this.world.app.clickButton('Add a Step');

      array.splice(randomIndex, 0, stepParameter);
    }

    const array2 = await this.getStepsArray();

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

    const array = await this.getStepsArray();
    const trashes = this.world.app.getElementsByClassName('delete-icon');
    const count = await trashes.count();

    const randomIndexes = [];
    for (let i = 0; i < numberOfSteps; i++) {
      randomIndexes.push(Math.floor(Math.random() * (count - 2 - i)));
    }

    for (const randomIndex of randomIndexes) {
      trashes.get(randomIndex + 1).click();
      this.world.app.getFirstVisibleButton('Delete').click();
      array.splice(randomIndex, 1);
    }

    const array2 = await this.getStepsArray();

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

  public async getStepsArray(): P<any> {
    const stepFactory = new StepFactory();
    const steps = this.world.app.getElementsByClassName('parent-step');

    const count = await steps.count();
    const stepsArray = new Array();

    for (let i = 1; i < (count - 1); i++) {
      steps.get(i).click();
      const title = this.world.app.getElementByCssSelector("span[class='parent-step active']");

      const text = await title.getText();
      const stepPage = stepFactory.getStep(text, '');

      await stepPage.initialize();

      stepsArray.push(stepPage.getParameter());
    }

    this.world.app.getFirstVisibleButton('Done').click();

    return stepsArray;
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

  //Kebab menu test, #553 -> part #548, #549.
  @when(/^clicks on the kebab menu icon of each available Integration and checks whether menu is visible and has appropriate actions$/)
  public clickOnAllKebabMenus(): P<any> {
    const integrationsListComponent = new IntegrationsListComponent();
    return integrationsListComponent.checkAllIntegrationsKebabButtons();
  }


  // Twitter search specification
  @then(/^she fills keywords field with random text to configure search action$/)
  public fillKeywords(): P<any> {

    const actionConfComponent = new IntegrationEditPage().actionConfigureComponent();
    const value = Utils.randomString(20, 'abcdefghijklmnopqrstuvwxyz');
    return actionConfComponent.fillKeywordsValueB(value);

  }

  /**
   * whether it's start or finish connection
   * @param type
   * @param callback
   */
  @then(/^she is prompted to select a "([^"]*)" connection from a list of available connections$/)
  public async verifyTypeOfConnection(type: string): P<any> {
    // Write code here that turns the phrase above into concrete actions

    const page = new IntegrationEditPage();

    const connection = await page.flowViewComponent().flowConnection(type);

    return expect(connection.isActive(), `${type} connection must be active`)
      .to.eventually.be.true;
  }

}
export = IntegrationSteps;
