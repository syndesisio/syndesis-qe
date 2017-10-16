import { element, by, ElementFinder } from 'protractor';
import { P } from '../../common/world';

/**
 * Created by jludvice on 13.3.17.
 */
export class ConnectionDetailPage {
  connectionDetailElem(): ElementFinder {
    return element(by.css('syndesis-connection-detail-info'));
  }

  connectionName(): P<string> {
    return this.connectionDetailElem().$('syndesis-editable-text').getText();
  }
}
