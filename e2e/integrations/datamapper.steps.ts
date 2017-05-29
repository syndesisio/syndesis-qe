import { binding, then } from 'cucumber-tsflow';
import { expect, P, World } from '../common/world';
import { DataMapperComponent } from './edit/datamapper.po';


@binding([World])
class DataMapperSteps {

  constructor(private world: World) {
  }

  @then(/^she is presented with data mapper ui$/)
  public dataMapperUIpresent(): P<any> {
    // Write code here that turns the phrase above into concrete actions


    const component = new DataMapperComponent();

    return expect(component.fieldsCount(), `data mapper ui must load and show fields count`)
      .to.eventually.be.greaterThan(5);
  }
}

export = DataMapperSteps;
