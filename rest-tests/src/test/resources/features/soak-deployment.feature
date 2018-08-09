@soak-deployment
@Ignore
Feature: soak deployment

  #scenario expects that web services are already deployed
  Scenario: create connections
    When import extensions from syndesis-extensions folder
      | syndesis-extension-delay |

    Given create "amq" connection from "amq-stability" template
    And create "number-creator" connection from "number-creator" template
    And create "number-guesser" connection from "number-guesser" template

  Scenario: first-number integration
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | new-number      |
    And add "number-creator" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path   | httpMethod |
      | create | GET        |
    When add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | first-number    | true       |

    And create integration with name: "create-new-number"
    And wait for integration with name: "create-new-number" to become active

  Scenario: first-guess integration
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | first-number    |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | guess | GET        |
    When add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | guesses         | true       |

    And create integration with name: "first-guess"
    And wait for integration with name: "first-guess" to become active


  Scenario: provide-feedback integration
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | guesses         |
    And add "number-creator" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path    | httpMethod |
      | compare | POST       |
    And add "Delay" extension step with "delay" action with properties:
      | delay |
      | 1000  |
    And add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | have-number     | true       |
    And add advanced filter step with "${bodyAs(String)} contains 'number'" expression
    And add log step
    And add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | stats-guessed   | true       |
    And create integration with name: "provide-feedback"
    And wait for integration with name: "provide-feedback" to become active


  Scenario: process feedback
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | have-number     |
    And add advanced filter step with "${bodyAs(String)} not contains 'number'" expression
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path   | httpMethod |
      | update | POST       |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | guess | GET        |

    And add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | guesses         | true       |
    And add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | stats-guesses   | true       |

    And create integration with name: "process-feedback"
    And wait for integration with name: "process-feedback" to become active

  Scenario: guessed-stats
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | stats-guessed   |
    And add "number-guesser" endpoint with connector id "http4" and "http4-invoke-url" action and with properties:
      | path  | httpMethod |
      | reset | GET        |
    And add log step
    When add "amq" endpoint with connector id "activemq" and "publish" action and with properties:
      | destinationType | destinationName | persistent |
      | queue           | new-number      | true       |
    And create integration with name: "stats-guessed"
    And wait for integration with name: "stats-guessed" to become active

  Scenario: guess-stats
    When add "amq" endpoint with connector id "activemq" and "subscribe" action and with properties:
      | destinationType | destinationName |
      | queue           | stats-guesses   |
    And add log step
    And add "log" endpoint with connector id "log" and "log-action" action and with properties:
      | showBody |
      | true     |
    And create integration with name: "stats-guess"
    And wait for integration with name: "stats-guess" to become active


