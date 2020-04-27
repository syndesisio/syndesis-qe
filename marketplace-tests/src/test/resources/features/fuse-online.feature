@operator
@operator-fuse-online
@manual
Feature: Fuse Online operator

  @operator-fuse-online-check-operator
  Scenario: Check that operator pod is alive

    When go to openshift main page
    And open installed operators page
    And select correct namespace in dropdown list

    Then check that operator "Red Hat Integration - Fuse Online" is in status "Succeeded"
    And check by api that pod with name "syndesis-operator" exists in namespace and is in phase "Running"

