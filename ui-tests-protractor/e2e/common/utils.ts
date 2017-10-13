import { ElementFinder } from 'protractor';
import { P } from '../common/world';
import { log } from '../../src/app/logging';

export class Utils {

  static randomString(length: number, chars: string): string {
    let result = '';
    for (let i = length; i > 0; --i) {
      result += chars[Math.floor(Math.random() * chars.length)];
    }
    return result;
  }

  /**
 * Fill form with given data. It will look for ui element for every map entry.
 * @param data key,value data. Key is used for element lookup.
 * @param parrentElement search inputs in child elements of this one
 * @param using means what kind of identificator.it is.
 * @returns {Promise<[void,T2,T3,T4,T5,T6,T7,T8,T9,T10]>}
 */
  static fillForm(data: Map<string, string>, parrentElement: ElementFinder, using: string): P<void[]> {

    const promises: P<void>[] = [];

    data.forEach((value, key) => {
      log.debug(`filling form item ${key} => ${value}`);
      promises.push(parrentElement.$(`input[${using}="${key}"`).sendKeys(value));
    });
    return P.all(promises);
  }

  /**
   * Delays stay on the page.
   * @param ms delay in miliseconds.
   */
  static delay(ms: number): P<any> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }



}

