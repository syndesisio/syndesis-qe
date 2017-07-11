import { SyndesisComponent } from '../../common/common';
import { $, browser, ElementFinder, ExpectedConditions} from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';

export class DataMapperComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-data-mapper-host').$('data-mapper');
  }

  /**
   * Find proper source and target datamapper columns.
   * @returns {Promise<ElementFinder[]>} [source, target] div elements
   */
  private async dataMapperColumns(): P<ElementFinder[]> {
    log.info('searching for columns');

    const loadedSelector = this.rootElement().$('div.card-pf-heading.fieldsCount');
    browser.wait(ExpectedConditions.presenceOf(loadedSelector), 1000, 'wait for datamapper to load');

    log.info(`datamapper loaded`);
    const elements = await this.rootElement().$$('div.docDef');
    log.info(`found ${elements.length} datamapper columns`);
    // log.info(`found ${elements.length}`);
    if (elements.length !== 2) {
      throw new Error(`There should be 2 datamapper columns. Found ${elements.length}`);
    }
    return elements;
  }

  /**
   * Eventually returns count of found datamapper fields.
   *
   * @returns {wdpromise.Promise<boolean>}
   */
  async fieldsCount(): P<number> {
    const columns = await this.dataMapperColumns(); // source column
    const countElement = columns[0].$('div.card-pf-heading.fieldsCount');

    const count = await countElement.getText();
    // "77 fields" > 77
    const regex = /(\d+) /;
    const found = regex.exec(count);
    if (found.length !== 2) {
      throw new Error(`failed to get files number from ${count}`);
    }
    return Number.parseInt(found[1]);
  }


  async createMapping(source: string, target: string): P<any> {
    log.info(`creating mapping from ${source} to ${target}`);
    const columns = await this.dataMapperColumns();
    const src = columns[0];
    const dest = columns[1];


    return P.all([
      this.selectMapping(source, src),
      this.selectMapping(target, dest),
    ]);
  }

  /**
   * Filter datamapper field element by it's name
   * @param name name to find
   * @param fields fields array
   * @returns {Promise<any>} element or promise reject
   */
  async findFieldByName(name: string, fields: ElementFinder[]): P<ElementFinder | any> {
    log.info(`searching field named ${name}`);
    for (const f of fields) {
      const fieldName = await this.fieldName(f);
      if (fieldName === name) {
        log.info(`field ${name} found`);
        return f;
      }
    }
    log.warn(`field ${name} not found between ${fields.length} fields, rejecting`);
    return P.reject(`Field ${name} not found in given ${fields.length} fields`);
  }

  /**
   *
   * @param mappingName for instance "User.ScreenName"
   * @param containerElement start searching mapping fields from here
   * @returns {Promise<void>}
   */
  async selectMapping(mappingName: string, containerElement: ElementFinder): P<any> {
    const path = mappingName.split('.')
      .map(item => item.trim());

    let fields = await containerElement.$$('document-field-detail');
    log.info(`source has ${fields.length} fields`);

    let nextField: ElementFinder;
    for (const p of path) {
      nextField = await this.findFieldByName(p, fields);
      // click on it to expand or select
      // find correct field from list
      log.info(`Clicking on field ${p}`);
      await nextField.click();
      // find all subfields for next iteration
      fields = await nextField.$$('document-field-detail');
    }
  }

  /**
   * Get string name from given datamapper field element.
   * @param fieldElement element to capture name
   * @returns {string} field name
   */
  fieldName(fieldElement: ElementFinder): P<string> {
    const nameElement = fieldElement.$('div.fieldDetail > div > label');
    return nameElement.getText();
  }

  /**
   * Expand field and return list of child elements
   * @param field
   * @returns {Promise<ElementFinder[]>} list of child elements or empty
   */
  async expandField(field: ElementFinder): P<ElementFinder[]> {
    const isParrent = await field.$('div.parentField').isPresent();
    if (!isParrent) {
      log.info(`field ${field} is not parent field`);
      return [];
    }

    field.click();

    const children = await field.$('div.childrenFields').$$('document-field-detail');
    log.info(`field ${field} has ${children.length} child fields`);

    return children;
  }
}
