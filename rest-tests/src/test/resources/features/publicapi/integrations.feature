# @sustainer: mkralik@redhat.com

@rest
@publicapi
@publicapi-integrations
Feature: Public API - integrations point

  Background: Prepare
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API
    And delete all tags in Syndesis
    And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
    And add log step
    And create new integration with name: "integration1" and desiredState: "Unpublished"
    And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
    And add log step
    And create new integration with name: "integration2" and desiredState: "Unpublished"
    And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
    And add log step
    And create new integration with name: "integrationWithoutTags" and desiredState: "Unpublished"
    Then check that integration integration1 doesn't contain any tag
    And check that integration integration2 doesn't contain any tag
    And check that integration integrationWithoutTags doesn't contain any tag

  # GET /public​/integrations​/{id}​/tags
  @add-tags-to-integration
  Scenario: Add and get tags in integration
    When add tags to Syndesis
      | tag1 | tag2 | tag3 |
    And add tags to integration integration1
      | tag1 | tag2 | tag3 |

    Then check that integration integration1 contains exactly tags
      | tag1 | tag2 | tag3 |
    And check that integration integration2 doesn't contain any tag
    And check that integration integrationWithoutTags doesn't contain any tag
    And check that Syndesis contains exactly tags
      | tag1 | tag2 | tag3 |

  # PATCH ​/public​/integrations​/{id}​/tags
  @add-tags-on-integration-not-remove-previous
  Scenario: Update tags on integration without remove previous
    When add tags to Syndesis
      | tag1 | tag2 | tagFor2 | tag3 | tag4 |
    And add tags to integration integration1
      | tag1 | tag2 |
    And add tags to integration integration2
      | tagFor2 |
    Then check that integration integration1 contains exactly tags
      | tag1 | tag2 |

    When add tags to integration integration1
      | tag3 | tag4 |
    Then check that integration integration1 contains exactly tags
      | tag1 | tag2 | tag3 | tag4 |
    And check that integration integration2 contains exactly tags
      | tagFor2 |
    And check that Syndesis contains exactly tags
      | tag1 | tag2 | tagFor2 | tag3 | tag4 |
    And check that integration integrationWithoutTags doesn't contain any tag

  # PUT ​/public​/integrations​/{id}​/tags
  @update-tags-on-integration-remove-previous
  Scenario: Update tags on integration uncheck previous
    When add tags to Syndesis
      | tag1 | tag2 | tagFor2 | tag3 | tag4 |
    And add tags to integration integration1
      | tag1 | tag2 |
    And add tags to integration integration2
      | tagFor2 |
    Then check that integration integration1 contains exactly tags
      | tag1 | tag2 |

    When update tags on integration integration1
      | tag3 | tag4 |
    Then check that integration integration1 contains exactly tags
      | tag3 | tag4 |
    And check that integration integration2 contains exactly tags
      | tagFor2 |
    And check that Syndesis contains exactly tags
      | tagFor2 | tag3 | tag4 | tag1 | tag2 |
    And check that integration integrationWithoutTags doesn't contain any tag

  # DELETE ​/public​/integrations​/{id}​/tags​/{env}
  @delete-tag-from-integration
  Scenario: Delete tag from integration
    When add tags to Syndesis
      | tagForDelete | tag2 | tag4 |
    And add tags to integration integration1
      | tagForDelete | tag2 |
    And add tags to integration integration2
      | tagForDelete | tag4 |
    And delete tag tagForDelete from the integration integration1

    Then check that integration integration1 contains exactly tags
      | tag2 |
    And check that integration integration1 doesn't contain tag tagForDelete
    # tagForDelete still have to be in integration2 because it was deleted only from integration 1
    And check that integration integration2 contains exactly tags
      | tagForDelete | tag4 |
    And check that integration integrationWithoutTags doesn't contain any tag

    When delete tag tagForDelete from the integration integration2
    Then check that integration integration1 contains exactly tags
      | tag2 |
    And check that integration integration2 contains exactly tags
      | tag4 |
    And check that integration integration2 doesn't contain tag tagForDelete
    And check that integration integrationWithoutTags doesn't contain any tag
    # tagForDelete was deleted from integration2, no integration contains this tag however it have to be still in the Syndesis gh-
    And check that tag with name tagForDelete is in the tag list
    And check that Syndesis contains exactly tags
      | tag2 | tag4 | tagForDelete |

  # PUT​ /public​/integrations​/{id}​/deployments​/stop - stop integration
  # POST /public​/integrations​/{id}​/deployments - start / redeploy
  # GET /public​/integrations​/{id}​/state
  @stop-integration
  Scenario: Stop integration
    Then check that state of the integration integration1 is Unpublished

    When set integration with name: "integration1" to desiredState: "Published"
    And wait for integration with name: "integration1" to become active
    Then check that state of the integration integration1 is Published

    When stop integration integration1
    Then check that state of the integration integration1 is Unpublished
    And validate integration: "integration1" pod scaled to 0

  # POST /public​/integrations​/{id}​/deployments - start / redeploy
  # GET /public​/integrations​/{id}​/state
  @deploy-redeploy-integration
  Scenario: Deploy and redeploy integration
    Then check that state of the integration integration2 is Unpublished

    When deploy integration integration2 via PublicApi
    And wait for integration with name: "integration2" to become active
    Then check that state of the integration integration2 is Published

    When deploy integration integration2 via PublicApi
    Then check that state of the integration integration2 is Pending
    When wait for integration with name: "integration2" to become active
    Then check that state of the integration integration2 is Published
    And check that verion of the integration integration2 is 2

  # GET ​/public​/integrations​/{env}​/export.zip
  # POST ​/public​/integrations
  @export-import-integrations
  @ENTESB-11653
  Scenario: Export and import integrations according to tag
    When add tags to Syndesis
      | tag1 | tag12 | tag3 | anotherTag1 | anotherTag2 | importedTag | importedTag2 |
    And add tags to integration integration1
      | tag1 | tag12 |
    And add tags to integration integration2
      | tag3 | tag12 |
    And add tags to integration integrationWithoutTags
      | anotherTag1 | anotherTag2 |
    And export integrations with tag tag12 as "export12.zip"

    # don't export integrations which were already exported
    Then verify that status code after export integrations with tag tag12 is 204
    # after use tag12 for new integration, the export endpoint should export only that new integration
    When add tags to integration integrationWithoutTags
      | tag12 |
    And export integrations with tag tag12 as "export12_forWithoutTagIntegration.zip"
    # revert integrationWithoutTag (remove tag12)
    And delete tag tag12 from the integration integrationWithoutTags

    And delete integration with name integration1
    And delete integration with name integration2

    # Test export12.zip
    Then verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist
    And check that integration integrationWithoutTags contains exactly tags
      | anotherTag1 | anotherTag2 |
    And check that Syndesis contains exactly tags
      | tag1 | tag12 | tag3 | anotherTag1 | anotherTag2 | importedTag | importedTag2 |

    When delete tag with name tag1
    And delete tag with name tag12
    And delete tag with name tag3
    Then check that Syndesis contains exactly tags
      | anotherTag1 | anotherTag2 | importedTag | importedTag2 |

    When import integrations with tag importedTag with name "export12.zip"
    Then check that Syndesis contains exactly tags
      | tag1 | tag12 | tag3 | anotherTag1 | anotherTag2 | importedTag | importedTag2 |
    And check that integration integration1 contains exactly tags
      | tag1 | tag12 | importedTag |
    And check that integration integration2 contains exactly tags
      | tag3 | tag12 | importedTag |
    And check that integration integrationWithoutTags contains exactly tags
      | anotherTag1 | anotherTag2 |

    # Test export12_forWithoutTagIntegration.zip
    When delete integration with name integration1
    And delete integration with name integration2
    And delete integration with name integrationWithoutTags
    Then verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist
    And verify that integration with name "integrationWithoutTags" doesn't exist
    When import integrations with tag importedTag2 with name "export12_forWithoutTagIntegration.zip"
    Then check that integration integrationWithoutTags contains exactly tags
      | anotherTag1 | anotherTag2 | tag12 | importedTag2 |
    # check that only integration3 was imported
    And verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist

  # GET ​/public​/integrations​/{env}​/export.zip?all=true
  # POST ​/public​/integrations
  @export-import-all-integrations
  Scenario: Export and import all integrations from Syndesis according to tag
    When add tags to Syndesis
      | tag1 | tag12 | tag3 | anotherTag1 | anotherTag2 | importedTag |
    And add tags to integration integration1
      | tag1 | tag12 |
    And add tags to integration integration2
      | tag3 | tag12 |
    And add tags to integration integrationWithoutTags
      | anotherTag1 | anotherTag2 |
    And export integrations with tag tag12 and others as "exportAll.zip"
    # After export all integrations, the all integrations are marked with the particular tag "tag12"
    Then check that integration integrationWithoutTags contains exactly tags
      | anotherTag1 | anotherTag2 | tag12 |

    When clean application state
    Then check that Syndesis doesn't contain any tag

    When add tags to Syndesis
      | importedTag |
    And import integrations with tag importedTag with name "exportAll.zip"
    Then check that Syndesis contains exactly tags
      | tag1 | tag12 | tag3 | anotherTag1 | anotherTag2 | importedTag |
    And check that integration integration1 contains exactly tags
      | tag1 | tag12 | importedTag |
    And check that integration integration2 contains exactly tags
      | tag3 | tag12 | importedTag |
    And check that integration integrationWithoutTags contains exactly tags
      | anotherTag1 | anotherTag2 | tag12 | importedTag |

  # GET ​/public​/integrations​/{env}​/export.zip?ignoreTimestamp=true
  # POST ​/public​/integrations
  @export-import-integrations-ignore-timestamp
  @ENTESB-12885
  Scenario: Export and import integrations according to tag and ignoring timestamp
    When add tags to Syndesis
      | tag1 | tag12 | tag2 | importedTag | importedTag2 |
    And add tags to integration integration1
      | tag1 | tag12 |
    And add tags to integration integration2
      | tag2 | tag12 |
    And export integrations with tag tag12 as "export12_a.zip"
    Then verify that status code after export integrations with tag tag12 is 204
    And export integrations with tag tag12 as "export12_b.zip" and ignore timestamp
    Then verify that status code after export integrations with tag tag12 is 200 when timestamp is ignored

    # Test export12_a.zip
    When delete integration with name integration1
    And delete integration with name integration2
    Then verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist

    When delete tag with name tag1
    And delete tag with name tag12
    And delete tag with name tag2
    Then check that Syndesis contains exactly tags
      | importedTag | importedTag2 |

    When import integrations with tag importedTag with name "export12_a.zip"
    Then check that integration integration1 contains exactly tags
      | tag1 | tag12 | importedTag |
    And check that integration integration2 contains exactly tags
      | tag2 | tag12 | importedTag |

    # Test export12_b.zip
    When delete integration with name integration1
    And delete integration with name integration2
    Then verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist

    When delete tag with name tag1
    And delete tag with name tag12
    And delete tag with name tag2
    Then check that Syndesis contains exactly tags
      | importedTag | importedTag2 |

    When import integrations with tag importedTag2 with name "export12_b.zip"
    Then check that integration integration1 contains exactly tags
      | tag1 | tag12 | importedTag2 |
    And check that integration integration2 contains exactly tags
      | tag2 | tag12 | importedTag2 |

  @export-import-and-deploy
  Scenario: Export, import and deploy integration via PublicApi
    When add tags to Syndesis
      | tag1 | tag12 | tag2 | importedTag |
    And add tags to integration integration1
      | tag1 | tag12 |
    And add tags to integration integration2
      | tag2 | tag12 |
    And export integrations with tag tag12 as "export12_deploy.zip"

    And delete integration with name integration1
    And delete integration with name integration2
    Then verify that integration with name "integration1" doesn't exist
    And verify that integration with name "integration2" doesn't exist

    When import integrations with tag importedTag with name "export12_deploy.zip"
    Then check that integration integration1 contains exactly tags
      | tag1 | tag12 | importedTag |
    And check that integration integration2 contains exactly tags
      | tag2 | tag12 | importedTag |

    When deploy integration integration1 via PublicApi
    And wait for integration with name: "integration1" to become active
    Then check that state of the integration integration1 is Published

  @export-import-integrations-with-connection-test
  Scenario: Export/import integration and check connection was imported to
    When add tags to Syndesis
      | tagAMQ |
    And deploy ActiveMQ broker
    And create ActiveMQ connection

    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "http-producer-get-input"
    And add log step
    And create new integration with name: "amqLog" and desiredState: "Unpublished"
    And add tags to integration amqLog
      | tagAMQ |
    And export integrations with tag tagAMQ as "exportAMQ.zip"

    And clean application state
    And add tags to Syndesis
      | tagAMQ |

    Then verify that integration with name "amqLog" doesn't exist
    And check that connection "Fuse QE ACTIVEMQ" doesn't exist

    When import integrations with tag tagAMQ with name "exportAMQ.zip"
    Then verify that integration with name "amqLog" exists
    And check that connection "Fuse QE ACTIVEMQ" exists
    And check that integration amqLog doesn't contains any warning

    When deploy integration amqLog via PublicApi
    And wait for integration with name: "amqLog" to become active
    Then check that state of the integration amqLog is Published
