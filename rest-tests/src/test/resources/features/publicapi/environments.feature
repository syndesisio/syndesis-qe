# @sustainer: mkralik@redhat.com

@rest
@publicapi
@publicapi-environments
Feature: Public API - environments point

  Background: Prepare
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API
    And delete all tags in Syndesis
    And create start DB periodic sql invocation action step with query "SELECT 'integration1'" and period "50000" ms
    And add log step
    And create new integration with name: "integration1" and desiredState: "Unpublished"
    And create start DB periodic sql invocation action step with query "SELECT 'integration1'" and period "50000" ms
    And add log step
    And create new integration with name: "integration2" and desiredState: "Unpublished"
    And create start DB periodic sql invocation action step with query "SELECT 'integration1'" and period "50000" ms
    And add log step
    And create new integration with name: "integrationWithoutTags" and desiredState: "Unpublished"
    Then check that integration integration1 doesn't contain any tag
    And check that integration integration2 doesn't contain any tag
    And check that integration integrationWithoutTags doesn't contain any tag

  # GET ​/public​/environments
  @get-all-tags
  Scenario: Get all tags from Syndesis
    When add tags to integration integration1
      | tag1 | tag2 |
    And add tags to integration integration2
      | tag3 | tag4 |

    Then check that Syndesis contains exactly tags
      | tag1 | tag2 | tag3 | tag4 |

  # GET ​/public​/environments?withUses=true
  @get-all-tag-with-usages
  Scenario: Get all tags with the number of usages
    When add tags to integration integration1
      | tag1 | tag2 |
    And add tags to integration integration2
      | tag1 |
    And add tag tag3 to Syndesis

    Then check that tag tag1 is used in 2 integrations
    And check that tag tag2 is used in 1 integrations
    And check that tag tag3 is used in 0 integrations

  # GET ​/public​/environments
  @gh-5828
  @reproducer
  @get-all-tags-without-duplicates
  Scenario: Get all tags from Syndesis without duplicates
    When add tags to integration integration1
      | tag1 | tag2 |
    And add tag tag3 to Syndesis

    Then check that Syndesis contains exactly tags
      | tag1 | tag2 | tag3 |
    And check that integration integration1 contains exactly tags
      | tag1 | tag2 |

    When add tags to integration integration1
      | tag3 |

    Then check that integration integration1 contains exactly tags
      | tag1 | tag2 | tag3 |
    # check that getAll endpoint doesn't return duplicates
    And check that Syndesis contains exactly tags
      | tag1 | tag2 | tag3 |

  # POST /public​/environments​/{env}
  @add-new-tag
  Scenario: Add new tag to the Syndesis
    When add tag tagAlone to Syndesis
    Then check that Syndesis contains exactly tags
      | tagAlone |
    And check that integration integration1 doesn't contain tag tagAlone

  # PUT /public​/environments​/{env}
  @update-tag-globally
  Scenario: Update tag in all integration
    When add tags to integration integration1
      | tagOriginal | tag2 |
    And add tags to integration integration2
      | tagOriginal | tag4 |
    And update tag with name tagOriginal to tagRenamed
    Then check that integration integration1 contains exactly tags
      | tagRenamed | tag2 |
    And check that integration integration2 contains exactly tags
      | tagRenamed | tag4 |
    And check that tag with name tagRenamed is in the tag list
    And check that tag with name tagOriginal is not in the tag list
    And check that integration integration1 doesn't contain tag tagOriginal
    And check that integration integration2 doesn't contain tag tagOriginal
    And check that integration integrationWithoutTags doesn't contain any tag

    # update unassigned tag
    When add tag tagAloneOriginal to Syndesis
    And update tag with name tagAloneOriginal to tagAloneRenamed
    And check that tag with name tagAloneRenamed is in the tag list
    And check that tag with name tagAloneOriginal is not in the tag list
    And check that integration integration1 doesn't contain tag tagAloneRenamed
    And check that integration integration2 doesn't contain tag tagAloneRenamed
    And check that integration integrationWithoutTags doesn't contain any tag

  # DELETE /public​/environments​/{env}
  @delete-tag-globally
  Scenario: Delete tag from all integrations
    When add tags to integration integration1
      | tagForDelete | tag2 |
    And add tags to integration integration2
      | tagForDelete | tag4 |

    And delete tag with name tagForDelete

    Then check that tag with name tagForDelete is not in the tag list
    # tagForDelete was deleted from the whole syndesis
    And check that integration integration1 contains exactly tags
      | tag2 |
    And check that integration integration2 contains exactly tags
      | tag4 |
    And check that integration integrationWithoutTags doesn't contain any tag

    # delete unassigned tag
    When add tag tagAlone to Syndesis
    And delete tag with name tagAlone
    Then check that tag with name tagAlone is not in the tag list
