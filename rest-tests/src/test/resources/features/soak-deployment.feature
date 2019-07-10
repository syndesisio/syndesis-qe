# @sustainer: mmelko@redhat.com

@soak-deployment
@ignore
Feature: soak deployment

  #scenario expects that web services are already deployed
  Scenario: create connections
    When import extensions from syndesis-extensions folder
      | syndesis-extension-delay |

    Given create connection
      | connector    | activemq        |
      | connectionId | fuseqe-activemq |
      | account      | amq-stability   |
      | brokerUrl    | $ACCOUNT$       |
      | username     | $ACCOUNT$       |
      | password     | $ACCOUNT$       |
    And create connection
      | connector    | http           |
      | connectionId | number-creator |
      | account      | number-creator |
      | name         | number-creator |
      | baseUrl      | $ACCOUNT$      |
    And create connection
      | connector    | http           |
      | connectionId | number-guesser |
      | account      | number-guesser |
      | name         | number-guesser |
      | baseUrl      | $ACCOUNT$      |

  Scenario: first-number integration
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | new-number      |
    And add "number-creator" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path   | httpMethod |
      | create | GET        |
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | first-number    | true       |

    And create integration with name: "create-new-number"
    And wait for integration with name: "create-new-number" to become active

  Scenario: first-guess integration
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | first-number    |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | guess | GET        |
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | guesses         | true       |

    And create integration with name: "first-guess"
    And wait for integration with name: "first-guess" to become active


  Scenario: provide-feedback integration
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | guesses         |
    And add "number-creator" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path    | httpMethod |
      | compare | POST       |
    And add "delay" extension step with "delay" action with properties:
      | delay |
      | 1000  |
    And add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | have-number     | true       |
    And add advanced filter step with "${bodyAs(String)} contains 'number'" expression
    And add log step
    And add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | stats-guessed   | true       |
    And create integration with name: "provide-feedback"
    And wait for integration with name: "provide-feedback" to become active


  Scenario: process feedback
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | have-number     |
    And add advanced filter step with "${bodyAs(String)} not contains 'number'" expression
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path   | httpMethod |
      | update | POST       |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | guess | GET        |

    And add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | guesses         | true       |
    And add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | stats-guesses   | true       |

    And create integration with name: "process-feedback"
    And wait for integration with name: "process-feedback" to become active

  Scenario: guessed-stats
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | stats-guessed   |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | reset | GET        |
    And add log step
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | new-number      | true       |
    And create integration with name: "stats-guessed"
    And wait for integration with name: "stats-guessed" to become active

  Scenario: guess-stats
    When add "fuseqe-activemq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | stats-guesses   |
    And add log step
    And add "log" endpoint with connector id "log" and "log-action" action and with properties:
      | showBody |
      | true     |
    And create integration with name: "stats-guess"
    And wait for integration with name: "stats-guess" to become active
