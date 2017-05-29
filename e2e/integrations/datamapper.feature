Feature: Features testing data mapper
	in create integration flow

  Background:
    Given credentials for "Camilla"

  @datamapper
  Scenario: add data mapper step to integration
    # we'll push integration draft and start editing it
    Given application state "datamapper-integration-base.json"

    When "Camilla" navigates to the "Integrations" page
    Then Integration "DataMapper integration base" is present in integrations list

    When Camilla selects the "DataMapper integration base" integration
    Then she is presented with a visual integration editor for "DataMapper integration base"

    Then Camilla clicks on the "Add a step" link
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
