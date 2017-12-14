Feature: Common scenarios

  #    @clean-deploy
  #    Scenario: Clean & deploy
  #      Given clean default namespace
  #      When deploy Syndesis from template
  #      Then wait for Syndesis to become ready
  #
  @integrations-twitter-to-salesforce
  Scenario: TW - SF integration
    Given cleans TW to SF scenario
    And creates the TW connection using "twitter_listen" template
    And creates SF connection
    And creates TW mention step with "twitter-mention-connector" action
    And creates basic TW to SF filter step
    And creates TW to SF mapper step
    And creates SF step for TW SF test
    When creates TW to SF integration with name: "Twitter to salesforce contact rest test"
    Then waits for integration with name: "Twitter to salesforce contact rest test" to become active
    Then tweets a message "#backendTest Have you heard about Syndesis project? It is pretty amazing..."
    Then validates record is present in SF "#backendTest Have you heard about Syndesis project? It is pretty amazing..."

  @integrations-salesforce-to-db
  Scenario: SF - DB integration
    Given cleans before SF to DB, removes user with first name: "John" and last name: "Doe"
    And creates SF connection
    And creates SF step for SF DB test
    And creates SF DB mapper step
    And creates DB step
    When creates SF to DB integration with name: "Salesforce to DB rest test"
    Then waits for integration with name: "Salesforce to DB rest test" to become active
    Then creates SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
    Then validates SF to DB created new lead with first name: "John", last name: "Doe", email: "jdoe@acme.com"
	Then cleans after SF to DB, removes user with first name: "John" and last name: "Doe"
