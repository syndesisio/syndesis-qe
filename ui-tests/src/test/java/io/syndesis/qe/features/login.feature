Feature: Login

Scenario: Log in to syndesis
	Given "Camilla" logs into the Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
	Then  "Camilla" is presented with the Syndesis home page.