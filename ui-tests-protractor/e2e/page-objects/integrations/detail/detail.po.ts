import { element, by, ElementFinder, browser, ExpectedConditions } from 'protractor';
import { P } from '../../../common/world';

export class Actions {
    static START = 'Start Integration';
    static STOP = 'Stop Integration';
    static DELETE = 'Delete Integration';
    static EDIT = 'Edit Integration';
}

export class IntegrationDetailPageFactory {

  getDetailPage(integratinStatus: string): IntegrationDetailPage {
    if (integratinStatus == null) {
      return new IntegrationDetailPage;
    } else if (integratinStatus.toUpperCase() === 'ACTIVE') {
      return new IntegrationDetailPageActive();
    } else if (integratinStatus.toUpperCase() === 'INACTIVE') {
      return new IntegrationDetailPageInactive();
    } else if (integratinStatus.toUpperCase() === 'DELETED') {
      return new IntegrationDetailPageDeleted();
    } else if (integratinStatus.toUpperCase() === 'DRAFT') {
      return new IntegrationDetailPageDraft();
    } else if (integratinStatus.toUpperCase() === 'IN PROGRESS') {
      return new IntegrationDetailPageInProgress();
    }

    return null;
  }
}

export class IntegrationDetailPage {
  static readonly statusSelector = 'syndesis-integration-status';

  public readonly actionsSet:  Array<string> = [];

  rootElement(): ElementFinder {
    return element(by.css('syndesis-integration-detail-page'));
  }

  async getIntegrationName(): P<string> {
    const name = this.rootElement().element(by.css('h1'));
    await browser.wait(ExpectedConditions.visibilityOf(name), 6000, 'No integration title');
    return name.getText();
  }

  public deleteIntegration(): P<any> {
    const deleteButton = this.rootElement().element(by.buttonText('Delete Integration'));
    deleteButton.click();

    const okButton = element(by.buttonText('OK'));
    browser.wait(ExpectedConditions.visibilityOf(okButton), 30000, 'OK button not loaded in time');

    return okButton.click();
  }

  public editIntegration(): P<any> {
    const editIntegrationButton = this.rootElement().element(by.buttonText('Edit Integration'));
    return editIntegrationButton.click();
  }

  public done(): P<any> {
    const doneButton = this.rootElement().element(by.buttonText('Done'));
    return doneButton.click();
  }

  public getStatus(): P<string> {
    const status = this.rootElement().element(by.css(IntegrationDetailPage.statusSelector));
    return status.getText();
  }

  public performAction(action: string): P<any> {
    const actionButton = this.rootElement().element(by.buttonText(action));
    return actionButton.click();
  }

  public getActionButton(action: string): ElementFinder {
    return this.rootElement().element(by.buttonText(action));
  }
}

export class IntegrationDetailPageActive extends IntegrationDetailPage {
  public readonly actionsSet:  Array<string> = [Actions.STOP, Actions.DELETE, Actions.EDIT];

  public stopIntegration(): P<any> {
    const stopIntegrationButton = this.rootElement().element(by.buttonText(Actions.STOP));
    return stopIntegrationButton.click();
  }
}

export class IntegrationDetailPageInactive extends IntegrationDetailPage {
  public readonly actionsSet:  Array<string> = [Actions.START, Actions.DELETE, Actions.EDIT];

  public startIntegration(): P<any> {
    const startIntegrationButton = this.rootElement().element(by.buttonText(Actions.START));
    return startIntegrationButton.click();
  }
}

export class IntegrationDetailPageDeleted extends IntegrationDetailPage {
  public readonly actionsSet:  Array<string> = [Actions.EDIT];
}

export class IntegrationDetailPageDraft extends IntegrationDetailPage {
  public readonly actionsSet:  Array<string> = [Actions.START, Actions.DELETE, Actions.EDIT];

  public startIntegration(): P<any> {
    const startIntegrationButton = this.rootElement().element(by.buttonText(Actions.START));
    return startIntegrationButton.click();
  }
}

export class IntegrationDetailPageInProgress extends IntegrationDetailPage {
  public readonly actionsSet:  Array<string> = [Actions.DELETE, Actions.EDIT];
}
