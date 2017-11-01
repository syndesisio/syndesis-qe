Feature: Login

# doesn't work but looks like a test

Scenario: Log in to syndesis
	Given Camilla logs in on Openshift login page
	Then Camilla can see syndesis home page