import { SyndesisComponent } from '../../common/common';
import { $, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';

export class DataMapperComponent implements SyndesisComponent {
  rootElement(): ElementFinder {
    return $('syndesis-data-mapper-host').$('data-mapper');
  }


  /**
   * Eventually returns true if DataMapper UI is loaded
   * @returns {wdpromise.Promise<boolean>}
   */
  async fieldsCount(): P<number> {
    // we need to wait for datamapper element before proceeding with assertions
    const parentPresent = await this.rootElement().isPresent();
    const countElement = this.rootElement().element(by.css('div.card-pf-heading.fieldsCount'));

    const isPresent = await countElement.isPresent();
    const count = await countElement.getText();

    const regex = /(\d+) /;

    const found = regex.exec(count);
    if (found.length !== 2) {
      throw new Error(`failed to get files number from ${count}`);
    }
    return Number.parseInt(found[1]);
  }
}
