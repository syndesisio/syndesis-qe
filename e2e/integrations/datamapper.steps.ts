import { binding, then, when } from 'cucumber-tsflow';
import { expect, P, World } from '../common/world';
import { DataMapperComponent } from './edit/datamapper.po';

@binding([World])
class DataMapperSteps {
  constructor(private world: World) {}

  @when(/^she creates mapping from "([^"]*)" to "([^"]*)"$/)
  public createMapping(source: string, target: string): P<any> {
    const mapper = new DataMapperComponent();
    return mapper.createMapping(source, target);
  }

  @then(/^she is presented with data mapper ui$/)
  public dataMapperUIpresent(): P<any> {
    const component = new DataMapperComponent();

    return expect(
      component.fieldsCount(),
      `data mapper ui must load and show fields count`
    ).to.eventually.be.greaterThan(5);
  }
}

export = DataMapperSteps;
