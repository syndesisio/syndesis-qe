import { SyndesisComponent } from '../../common/common';
import { $, ElementFinder, promise } from 'protractor';
import { P } from '../../common/world';
import { log } from '../../../src/app/logging';
import Promise = promise.Promise;
/**
 * Created by jludvice on 4.4.17.
 */

export class TextEntity {
  readonly selector: ElementFinder;

  constructor(selector: ElementFinder) {
    this.selector = selector;
  }

  get(): P<string> {
    return this.selector.getText();
  }

  set(value: string | P<string>): P<void> {
    return P.resolve(value).then(v => this.selector.sendKeys(v));
  }
}

export class ConnectionConfigurationComponent implements SyndesisComponent {

  rootElement(): ElementFinder {
    return $('syndesis-connections-configure-fields');
  }

  /**
   * Fill connection details from given connection to ui
   * @param connection <key> : <value> object with data
   * @param parentElement fill input[name="<key>"] = <value> here
   * @returns {any} resolved promise once all filled
   */
  fillDetails(connection: any, parentElement: ElementFinder = this.rootElement()): P<void[]> {
    if (!connection) {
      return P.reject(`can't find any connection details in ${connection}`);
    }
    const promises: P<void>[] = [];
    Object.keys(connection).forEach(key => {
      log.info(`fill connection detail ${key} => ${connection[key]}`);
      promises.push(parentElement.$(`input[name="${key}"`).sendKeys(connection[key]));
    });
    return P.all(promises);
  }
}

export class ConnectionDetailsComponent implements SyndesisComponent {

  name = new TextEntity(this.rootElement().$('input[data-id="nameInput"]'));
  description = new TextEntity(this.rootElement().$('textarea[data-id="descriptionInput"]'));

  rootElement(): ElementFinder {
    return $('syndesis-connections-review');
  }
}

export class ConnectionCreatePage implements SyndesisComponent {

  rootElement(): ElementFinder {
    return $('syndesis-connection-create-page');
  }
}
