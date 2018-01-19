Feature: tw scenarios

  @integrations-tw-sf
  Scenario: TW - SF integration
	Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
	And clean all tweets
    And create the TW connection using "twitter_talky" template
    And create SF connection
    And create TW mention step with "twitter-mention-action" action
    And create basic TW to SF filter step
    And create mapper step using template: "twitter-salesforce"
    And create SF step for TW SF test
    When create integration with name: "Twitter to salesforce contact rest test"
    Then wait for integration with name: "Twitter to salesforce contact rest test" to become active
    Then check SF does not contain contact for tw accound: "twitter_talky"
    Then tweet a message "#backendTest Have you heard about Syndesis project? It is pretty amazing..."
    Then validate contact for TW account: "twitter_talky" is present in SF with description: "#backendTest Have you heard about Syndesis project? It is pretty amazing..."
	Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
	And clean all tweets
