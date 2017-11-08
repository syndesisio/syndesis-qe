import { ElementFinder, browser, ExpectedConditions } from 'protractor';
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
   * Delays stay on the page.
   * @param ms delay in miliseconds.
   */
  static delay(ms: number): P<any> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

