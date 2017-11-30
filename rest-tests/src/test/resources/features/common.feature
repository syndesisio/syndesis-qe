Feature: Common scenarios

@clean-deploy
Scenario: Clean & deploy
	Given clean default namespace
	When deploy Syndesis from template
	Then wait for Syndesis to become ready