Feature: tw scenarios

  @integrations-twitter-to-salesforce
  Scenario: TW - SF integration
    Given clean TW to SF scenario
    And create the TW connection using "twitter_listen" template
    And create SF connection
    And create TW mention step with "twitter-mention-connector" action
    And create basic TW to SF filter step
    And create TW to SF mapper step
    And create SF step for TW SF test
    When create TW to SF integration with name: "Twitter to salesforce contact rest test"
    Then wait for integration with name: "Twitter to salesforce contact rest test" to become active
    Then tweet a message "#backendTest Have you heard about Syndesis project? It is pretty amazing..."
    Then validate record is present in SF "#backendTest Have you heard about Syndesis project? It is pretty amazing..."
