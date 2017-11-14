Feature: Common scenarios

@clean-deploy
Scenario: Clean & deploy
	Given user cleans default namespace
	When user deploys Syndesis from template
	Then user waits for Syndesis to become ready