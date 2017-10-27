Feature: Login

# doesn't work but looks like a test

Scenario: Log in to syndesis
	Given Camilla logs in on Openshift login page
	When Camilla fills in her username and password
	And Logs in Openshift
	When Camilla updates account information
	Then Camilla can see syndesis home page