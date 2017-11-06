import { binding, then, when } from 'cucumber-tsflow';
import { expect, P, World } from '../common/world';
import { DataMapperComponent } from './edit/datamapper.po';
import { ActionConfigureComponent, IntegrationEditPage } from '../integrations/edit/edit.po';
import { log } from '../../src/app/logging';

import { browser, ElementFinder, ExpectedConditions } from 'protractor';

@binding([World])
class DataMapperSteps {

  constructor(private world: World) {
  }

  @when(/^she creates mapping from "([^"]*)" to "([^"]*)"$/)
  public createMapping(source: string, target: string): P<any> {

    const mapper = new DataMapperComponent();
    return mapper.createMapping(source, target);
  }

  @then(/^she is presented with data mapper ui$/)
  public dataMapperUIpresent(): P<any> {

    const component = new DataMapperComponent();

    return expect(component.fieldsCount(), `data mapper ui must load and show fields count`)
      .to.eventually.be.greaterThan(5);
  }

  @when(/^she selects "([^"]*)" from "([^"]*)" selector-dropdown$/)
  public async selectFromDropDownByElement(option: string, selectAlias: string): P<any> {
    const datamapperCommon = new DataMapperComponent();
    const selectElement = await datamapperCommon.getSelectByAlias(selectAlias);
    await browser.wait(ExpectedConditions.visibilityOf(selectElement), 6000, `Input ${selectAlias} not loaded in time`);
    return this.world.app.selectOption(selectElement, option);
  }

  @then(/^she fills "([^"]*)" selector-input with "([^"]*)" value$/)
  public async fillActionConfigureField(selectorAlias: string, value: string): P<any> {

    const actionConf = new ActionConfigureComponent();
    // const datamapperComponent = new DataMapperComponent();
    const datamapperCommon = new DataMapperComponent();
    const inputElement = await datamapperCommon.getInputByAlias(selectorAlias);
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input is not visible');
    return actionConf.fillInputByElement(inputElement, value);
  }

  /**
   * @param first parameter to be combined.
   * @param first_pos position of the first parameter in the final string
   * @param second parameter to be combined.
   * @param sec_pos position of the second parameter in the final string.
   * @param combined above two into this parameter.
   * @param separator used to estethically join first and second parameter.
   */
  // And she combines "FirstName" as "2" with "LastName" as "1" to "first_and_last_name" using "Space" separator
  @then(/^she combines "([^"]*)" as "([^"]*)" with "([^"]*)" as "([^"]*)" to "([^"]*)" using "([^"]*)" separator$/)
  public async combinePresentFielsWithAnother(first: string, first_pos: string, 
    second: string, sec_pos: string, combined: string, separator: string): P<any> {

    let inputElement: ElementFinder;
    let selectElement: ElementFinder;
    const world: World = new World();

    // Then she fills "FirstCombine" selector-input with "FirstName" value
    const actionConf = new ActionConfigureComponent();
    const datamapperCommon = new DataMapperComponent();
    inputElement = await datamapperCommon.getInputByAlias('FirstCombine');
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input FirstCombineis not visible');
    await actionConf.fillInputByElement(inputElement, first);

    // And she selects "Combine" from "ActionSelect" selector-dropdown
    selectElement = await datamapperCommon.getSelectByAlias('ActionSelect');
    await browser.wait(ExpectedConditions.visibilityOf(selectElement), 6000, `Select ActionSelect not loaded in time`);
    await this.world.app.selectOption(selectElement, 'Combine');

    // And she selects "Space" from "SeparatorSelect" selector-dropdown
    selectElement = await datamapperCommon.getSelectByAlias('SeparatorSelect');
    await browser.wait(ExpectedConditions.visibilityOf(selectElement), 6000, `Select SeparatorSelect not loaded in time`);
    await this.world.app.selectOption(selectElement, separator);

    // And clicks on the "Add Source" link 
    await world.app.clickLink('Add Source');

    // Then she fills "SecondCombine" selector-input with "LastName" value
    inputElement = await datamapperCommon.getInputByAlias('SecondCombine');
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input SecondCombine is not visible');
    await actionConf.fillInputByElement(inputElement, second);

    // And she fills "FirstCombinePosition" selector-input with "2" value
    inputElement = await datamapperCommon.getInputByAlias('FirstCombinePosition');
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input FirstCombinePosition is not visible');
    await actionConf.fillInputByElement(inputElement, first_pos);

    // And she fills "SecondCombinePosition" selector-input with "1" value
    inputElement = await datamapperCommon.getInputByAlias('SecondCombinePosition');
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input SecondCombinePosition is not visible');
    await actionConf.fillInputByElement(inputElement, sec_pos);

    // Then she fills "TargetCombine" selector-input with "first_and_last_name" value
    inputElement = await datamapperCommon.getInputByAlias('TargetCombine');
    await browser.wait(ExpectedConditions.visibilityOf(inputElement), 6000, 'Input TargetCombine is not visible');
    return actionConf.fillInputByElement(inputElement, combined);

  }
}

export = DataMapperSteps;
