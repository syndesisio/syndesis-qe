import { binding, then, when } from 'cucumber-tsflow';
import { expect, P, World } from '../common/world';
import { CallbackStepDefinition } from 'cucumber';
import { ConnectionDetailPage } from './detail/detail.po';
import { ConnectionsListComponent } from './list/list.po';
import { ConnectionCreatePage, ConnectionViewComponent } from './edit/edit.po';
import {log} from '../../src/app/logging';

// let http = require('http');

/**
 * Created by jludvice on 29.3.17.
 */


@binding([World])
class ConnectionSteps {

  constructor(private world: World) {
  }

  @then(/^Camilla is presented with "([^"]*)" connection details$/)
  public verifyConnectionDetails(connectionName: string, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    const page = new ConnectionDetailPage();
    expect(page.connectionName(), `Connection detail page must show connection name`)
      .to.eventually.be.equal(connectionName).notify(callback);
    // todo add more assertion on connection details page
  }

  @then(/^Camilla can see "([^"]*)" connection$/)
  public expectConnectionTitlePresent(connectionName: string, callback: CallbackStepDefinition): void {
    const listComponent = new ConnectionsListComponent();
    const connection = listComponent.getConnection(connectionName);
    expect(connection.isPresent(), `There should be present connection ${connectionName}`)
      .to.eventually.be.true.notify(callback);
  }

  @then(/^Camilla can not see "([^"]*)" connection anymore$/)
  public expectConnectionTitleNonPresent(connectionName: string, callback: CallbackStepDefinition): void {
    const listComponent = new ConnectionsListComponent();
    const connection = listComponent.getConnection(connectionName);
    expect(connection.isPresent(), `There shouldnt be a present connection ${connectionName}`)
      .to.eventually.be.false.notify(callback);
  }

  @then(/^she is presented with a connection create page$/)
  public editorOpened(callback: CallbackStepDefinition): void {
    const page = new ConnectionCreatePage();
    expect(page.rootElement().isPresent(), 'there must be edit page root element')
      .to.eventually.be.true.notify(callback);
  }

  @when(/^Camilla deletes the "([^"]*)" connection*$/)
  public deleteConnection(connectionName: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const listComponent = new ConnectionsListComponent();
    return listComponent.deleteConnection(connectionName);
  }

  @when(/^Camilla selects the "([^"]*)" connection.*$/)
  public selectConnection(connectionName: string): P<any> {
    // Write code here that turns the phrase above into concrete actions
    const listComponent = new ConnectionsListComponent();
    return listComponent.goToConnection(connectionName);
  }

  @when(/^type "([^"]*)" into connection name$/)
  public typeConnectionName(name: string): P<void> {
    // Write code here that turns the phrase above into concrete actions
    const connectionView = new ConnectionViewComponent();
    return connectionView.name.set(name);
  }

  @when(/^type "([^"]*)" into connection description/)
  public typeConnectionDescription(description: string): P<void> {
    // Write code here that turns the phrase above into concrete actions
    const connectionView = new ConnectionViewComponent();
    return connectionView.description.set(description);
  }

  @when(/^she fills "([^"]*)" connection details$/)
  public fillConnectionDetails(connectionName: string): P<any> {
    const connectionView = new ConnectionViewComponent();
    // return connectionView.fillForm(this.world.connectionDetails.get(connectionName));
    return connectionView.fillDetails(this.world.testConfig.connection[connectionName]);

  }

  //Kebab menu test, #553 -> part #550.
  @when(/^clicks on the kebab menu icon of each available connection$/)
  public clickOnAllKebabMenus(): P<any> {

    const connectionsListComp = new ConnectionsListComponent();

    return connectionsListComp.clickOnAllKebabButtons();

  }

  @then(/^she is presented with at least "(\d+)" connections$/)
  public connectionCount(connectionCount: number, callback: CallbackStepDefinition): void {
    // Write code here that turns the phrase above into concrete actions
    log.info(`should assert ${connectionCount}`);

    const page = new ConnectionsListComponent();
    expect(page.countConnections(), `There should be ${connectionCount} available`)
      .to.eventually.be.least(Number(connectionCount)).notify(callback);
  }

  //Kebab menu test, #553 -> part #550.
  @then(/^she can see unveiled kebab menu of all connections, each of this menu consist of "([^"]*)", "([^"]*)" and "([^"]*)" actions$/)
  public checkAllVisibleKebabMenus(action1: string, action2: string, action3: string): P<any> {

    const actions = [action1, action2, action3];

    const connectionsListComp = new ConnectionsListComponent();

    return connectionsListComp.checkAllKebabElementsAreDisplayed(true, actions);

  }
}


export = ConnectionSteps;
