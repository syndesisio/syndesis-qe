import { element, by, ElementFinder, browser, ExpectedConditions } from 'protractor';
import { P } from '../../common/world';

export class IntegrationDetailPage {
  rootElement(): ElementFinder {
    return element(by.css('syndesis-integration-detail-page'));
  }

  async getIntegrationName(): P<string> {
    const name = this.rootElement().element(by.css('h1'));
    await browser.wait(ExpectedConditions.visibilityOf(name), 6000, 'No integration title');
    return name.getText();
  }
}
