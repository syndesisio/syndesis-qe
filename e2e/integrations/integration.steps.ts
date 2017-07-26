/**
 * Created by jludvice on 8.3.17.
 */
import { binding, then, when } from 'cucumber-tsflow';
import { CallbackStepDefinition } from 'cucumber';
import { expect, P, World } from '../common/world';
import { IntegrationAddStepPage, IntegrationEditPage, ListActionsComponent, StepFactory } from '../integrations/edit/edit.po';
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
    // Write code here that turns the phrase above into concrete actions
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
    return this.world.app.clickDeleteIntegration(integrationName, listComponent.rootElement());
  }

  @when(/^she selects "([^"]*)" integration step$/)
  public addStep (stepName: string): P<any> {
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
  public fillStepConfiguration (stepType: string, parameter: string): P<any> {
    const stepFactory = new StepFactory();
    const page = stepFactory.getStep(stepType, parameter);
    return page.fillConfiguration();
  }

  @then(/^she adds "([^"]*)" random steps and then check the structure$/)
  public addRandomStepsAndCheckRest (numberOfSteps: number): void {

    this.getStepsArray().then((array) => {
      this.world.app.clickButton('Next');
      this.world.app.clickButton('Add a Step');

      const links = this.world.app.getLinks('Add a step');

      links.count().then((count) => {
        log.info(`links "${count}"`);

        const randomIndexes = [];
        for (let i = 0; i < numberOfSteps; i++) {
          randomIndexes.push(Math.floor((Math.random() * count)));
        }
        return randomIndexes;
      }).then((randomIndexes) => {
        const page = new IntegrationAddStepPage();

        for (const randomIndex of randomIndexes) {
          links.get(randomIndex).click();

          const steType = 'Basic Filter';
          const stepParameter = 'Any, path' + randomIndex + ', Does Not Contain, value' + randomIndex;

          page.addStep(steType);
          const stepFactory = new StepFactory();
          const stepPage = stepFactory.getStep(steType, stepParameter);
          stepPage.fillConfiguration();

          this.world.app.clickButton('Next');
          this.world.app.clickButton('Add a Step');

          array.splice(randomIndex, 0, stepParameter);
        }
        this.getStepsArray().then((array2) => {
          for (let i = 0; i < array2.length; i++) {
            log.info(`assserting "${array[i]}" and "${array2[i]}"`);
            expect(array[i]).to.be.equal(array2[i]);
          }
        });
      });
    });
  }

  @then(/^she delete "([^"]*)" random steps and check rest$/)
  public deleteRandomStepsAndCheckRest (numberOfSteps: number): void {

    this.getStepsArray().then((array) => {
      const trashes = this.world.app.getElementsByClassName('delete-icon');

      trashes.count().then((count) => {
        const randomIndexes = [];
        for (let i = 0; i < numberOfSteps; i++) {
          randomIndexes.push(Math.floor((Math.random() * (count - 2 - i))));
        }
        return randomIndexes;
      }).then((randomIndexes) => {
        for (const randomIndex of randomIndexes) {
          trashes.get(randomIndex + 1).click();
          this.world.app.getFirstVisibleButton('Delete').click();
          array.splice(randomIndex, 1);
        }
        this.getStepsArray().then((array2) => {
          for (let i = 0; i < array.length; i++) {
            log.info(`assserting "${array[i]}" and "${array2[i]}"`);
            expect(array[i]).to.be.equal(array2[i]);
          }
        });
      });
    });
  }

  public getStepsArray (): P<any> {
    const stepFactory = new StepFactory();
    const steps = this.world.app.getElementsByClassName('parent-step');

    return steps.count().then((count) => {
      const stepsArray = new Array();
      for (let i = 1; i < (count - 1); i++) {
        steps.get(i).click();
        const title = this.world.app.getElementByCssSelector("span[class='parent-step active']");

        title.getText().then((text) => {
          const stepPage = stepFactory.getStep(text, '');

          stepPage.initialize().then(() => {
            stepsArray.push(stepPage.getParameter());
          });
        });
      }
      return stepsArray;
    });
  }

  @then(/^she is presented with an actions list$/)
  public expectActionListIsPresent(): void {
      const page = new ListActionsComponent();
      browser.wait(ExpectedConditions.visibilityOf(page.rootElement()), 5000, 'Actions List not loaded');
      expect(page.rootElement().isDisplayed(), 'There must be action list loaded')
        .to.eventually.be.true;
  }
}


export = IntegrationSteps;
