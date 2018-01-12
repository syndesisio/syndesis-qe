Feature: sf scenarios

  @integrations-salesforce-to-db
  Scenario: SF - DB integration
    Given clean before SF to DB, removes user with first name: "John" and last name: "Doe"
    And create SF connection
    And create SF step for SF DB test
    And create SF DB mapper step
    And create DB step
    When create integration with name: "Salesforce to DB rest test"
    Then wait for integration with name: "Salesforce to DB rest test" to become active
    Then create SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
    Then validate SF to DB created new lead with first name: "John", last name: "Doe", email: "jdoe@acme.com"
    Then clean after SF to DB, removes user with first name: "John" and last name: "Doe"
