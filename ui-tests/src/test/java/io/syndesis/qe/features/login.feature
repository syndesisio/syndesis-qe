Feature: Login

Scenario: Log in to syndesis
	Given "Camilla" logs into the Syndesis.
	Given clean application state
	Then  "Camilla" is presented with the Syndesis home page.