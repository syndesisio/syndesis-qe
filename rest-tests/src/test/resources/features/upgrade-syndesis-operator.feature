# @sustainer: avano@redhat.com

# For testing upstream, only variable needed is the syndesis.config.install.version for the "current" version,
# the "previous" version will be determined from the docker hub
# For testing prod, you need to specify:
#   - syndesis.upgrade.previous.version - maven version of "previous" version
#   - (standard for prod build) syndesis.version - maven version of "current" version
#   - (standard for prod build) syndesis.config.build.properties.url - for deploying "current" operator when doing an upgrade


-Dsyndesis.config.build.properties.url=/tmp/raw.txt -Dsyndesis.upgrade.previous.version=1.8.6.fuse-750001-redhat-00002 -Dsyndesis.version=1.9.0.fuse-760016-redhat-00001

@ENTESB-12355
Feature: Syndesis Upgrade Using Operator
  @syndesis-upgrade
  Scenario: Syndesis Upgrade - basic
    Given prepare upgrade
      And clean default namespace
      And deploy Syndesis
      And wait for Syndesis to become ready
      And verify syndesis "previous" version
    When perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
      And sleep for jenkins delay or "180" seconds
      # Needed until https://issues.redhat.com/browse/ENTESB-12923 is fixed
      And rollout
      # Just to be sure for rollout that everything started to roll out
      And sleep for jenkins delay or "60" seconds
      And wait for Syndesis to become ready
      And verify syndesis "upgraded" version
      And check that pull secret is linked in the service accounts

  Scenario: Syndesis Upgrade - with integrations
    Given prepare upgrade
      And clean default namespace
      And deploy Syndesis
      And wait for Syndesis to become ready
      And verify syndesis "previous" version
    # Not needed for the integration, just to check if it is present after upgrade
    When import extension from path "./src/test/resources/extensions/set-sqs-group-id-extension-1.0-SNAPSHOT.jar"
      And inserts into "contact" table
      | X | Y | Z | db |
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add a split step
      And create basic filter step for "last_name" with word "Y" and operation "contains"
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
    Then create integration with name: "upgrade"
      And wait for integration with name: "upgrade" to become active
      And verify upgrade integration with task "X"
    When perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
    And sleep for jenkins delay or "180" seconds
    # Needed until https://issues.redhat.com/browse/ENTESB-12923 is fixed
      And rollout
      # Just to be sure for rollout that everything started to roll out
      And sleep for jenkins delay or "60" seconds
      And wait for Syndesis to become ready
      And verify syndesis "upgraded" version
      And verify upgrade integration with task "X"
      And verify that integration with name "upgrade" exists
      And check that extension "set-sqs-group-id-extension" exists
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
