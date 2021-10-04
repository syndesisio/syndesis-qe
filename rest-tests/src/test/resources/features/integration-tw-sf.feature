# @sustainer: tplevko@redhat.com

@rest
@twitter
@salesforce
@datamapper
Feature: Integration - Twitter

  @integration-tw-sf
  Scenario: Mention to Salesforce create object
    Given clean application state
    And delete contact from SF with email: "integrations-twitter-to-salesforce-rest"
    And clean all tweets in twitter_talky account
    And create Twitter connection using "Twitter Listener" account
    And create SalesForce connection
    And create TW mention step with "twitter-mention-action" action
    And create basic filter step for "text" with word "#backend" and operation "contains"

    And start mapper definition with name: "mapping 1"
    Then SEPARATE using Step 1 and strategy "Space" and source "/user/name" into targets
        | /FirstName | /LastName |
    Then MAP using Step 1 and field "/user/screenName" to "/TwitterScreenName__c"
    Then MAP using Step 1 and field "/text" to "/Email"

    When create SF "create-sobject" action step with properties
        | sObjectName | Contact |
    And create integration with name: "Twitter to salesforce contact rest test"
    And wait for integration with name: "Twitter to salesforce contact rest test" to become active
    Then check SF "does not contain" contact with a email: "integrations-twitter-to-salesforce-rest"
    When tweet a message from twitter_talky to "Twitter Listener" with text "integrations-twitter-to-salesforce-rest"
    Then check SF "contain" contact with a email: "integrations-twitter-to-salesforce-rest"
    When clean application state
    And delete contact from SF with email: "integrations-twitter-to-salesforce-rest"
    And clean all tweets in twitter_talky account
