# @sustainer: avano@redhat.com

# For testing upstream, only variable needed is the syndesis.config.install.version for the "current" version,
# the "previous" version will be determined from the docker hub
# For testing prod, you need to specify:
#   - syndesis.upgrade.previous.version - maven version of "previous" version
#   - (standard for prod build) syndesis.version - maven version of "current" version
#   - (standard for prod build) syndesis.config.build.properties.url - for deploying "current" operator when doing an upgrade

@ENTESB-12355
@ENTESB-12534
@syndesis-upgrade
Feature: Syndesis Upgrade Using Operator
  @syndesis-upgrade-basic
  Scenario: Syndesis Upgrade - basic
    Given prepare upgrade
      And clean default namespace
      And deploy previous Syndesis CR "syndesis-cr-previous.yaml"
      And wait for Syndesis to become ready
      And verify syndesis "previous" version
    When perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
      And sleep for jenkins delay or "180" seconds
      And wait for Syndesis to become ready
      And verify syndesis "upgraded" version
      And check that pull secret is linked in the service accounts

  @syndesis-upgrade-advanced
  Scenario: Syndesis Upgrade - with integrations
    Given prepare upgrade
      And clean default namespace
      And deploy previous Syndesis CR "syndesis-cr-previous.yaml"
      And wait for Syndesis to become ready
      And verify syndesis "previous" version
    Given deploy HTTP endpoints
    # Not needed for the integration, just to check if it is present after upgrade
    When import extension from path "./src/test/resources/extensions/set-sqs-group-id-extension-1.0-SNAPSHOT.jar"
    And create HTTP connection
    And create HTTP "OPTIONS" step with period "5" "SECONDS"
    And add log step
    And create integration with name: "upgrade"
    Then wait for integration with name: "upgrade" to become active
    And verify upgrade integration "upgrade"

    When perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
    And sleep for jenkins delay or "180" seconds
    # Needed until https://issues.redhat.com/browse/ENTESB-12923 is fixed
      And rollout
      # Just to be sure for rollout that everything started to roll out
      And sleep for jenkins delay or "60" seconds
      And wait for Syndesis to become ready
      And verify syndesis "upgraded" version
      And verify that integration with name "upgrade" exists
      And verify upgrade integration "upgrade"
      And check that extension "set-sqs-group-id-extension" exists
    When rebuild integration with name "upgrade"
    Then wait for integration with name: "upgrade" to become active
    And verify upgrade integration "upgrade"
    When deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "upgrade-after-in"
      And clean destination type "queue" with name "upgrade-after-out"
      And create ActiveMQ "subscribe" action step with destination type "queue" and destination name "upgrade-after-in"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "upgrade-after-out"
      And create integration with name: "amq-amq-upgrade"
    Then wait for integration with name: "amq-amq-upgrade" to become active
    When publish message with content "Hello upgrade" to "queue" with name "upgrade-after-in"
    Then verify that JMS message with content 'Hello upgrade' was received from "queue" "upgrade-after-out"

  @ENTESB-13518
  @syndesis-upgrade-external-db
  Scenario: Syndesis upgrade - external DB
    Given prepare upgrade
      And clean default namespace
    When deploy custom database
      And deploy previous Syndesis CR "syndesis-cr-previous-externaldb.yaml"
    Then wait for Syndesis to become ready
      And verify syndesis "previous" version
      And check that deployment config "syndesis-db" does not exist
    Given deploy HTTP endpoints
    # Not needed for the integration, just to check if it is present after upgrade
    When import extension from path "./src/test/resources/extensions/set-sqs-group-id-extension-1.0-SNAPSHOT.jar"
      And create HTTP connection
      And create HTTP "OPTIONS" step with period "5" "SECONDS"
      And add log step
      And create integration with name: "upgrade"
    Then wait for integration with name: "upgrade" to become active
      And verify upgrade integration "upgrade"
    When perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
      And sleep for jenkins delay or "180" seconds
      # Needed until https://issues.redhat.com/browse/ENTESB-12923 is fixed
      And rollout
        # Just to be sure for rollout that everything started to roll out
      And sleep for jenkins delay or "60" seconds
      And wait for Syndesis to become ready
      And check that deployment config "syndesis-db" does not exist
      And verify syndesis "upgraded" version
      And verify that integration with name "upgrade" exists
      And verify upgrade integration "upgrade"
      And check that extension "set-sqs-group-id-extension" exists
    When rebuild integration with name "upgrade"
    Then wait for integration with name: "upgrade" to become active
      And verify upgrade integration "upgrade"
    When deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "upgrade-external-after-in"
      And clean destination type "queue" with name "upgrade-external-after-out"
      And create ActiveMQ "subscribe" action step with destination type "queue" and destination name "upgrade-external-after-in"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "upgrade-external-after-out"
      And create integration with name: "amq-amq-external-upgrade"
    Then wait for integration with name: "amq-amq-external-upgrade" to become active
    When publish message with content "Hello upgrade" to "queue" with name "upgrade-external-after-in"
    Then verify that JMS message with content 'Hello upgrade' was received from "queue" "upgrade-external-after-out"
