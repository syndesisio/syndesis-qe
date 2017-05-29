# Syndesis E2E tests

To run tests first start local web server
```bash
yarn
yarn webdriver-manager update
```

#### GitHub credentials

Create `e2e/data/users.json` file with your GitHub login credentials
```json
{
  "users": {
    "camilla": {
      "username": "<GITHUB_USERNAME>",
      "password": "<GITHUB_PASSWORD>"
    }
  }
}
```
Alternatively you can define credentials through env variables
```bash
export SYNDESIS_CAMILLA_USERNAME='CamillaOfGit'
export SYNDESIS_CAMILLA_PASSWORD='topSecret'
```


#### Test environment configuration
Define env variable that points to your Syndesis UI web console
```bash
export SYNDESIS_UI_URL='https://<SYNDESIS_UI_URL>'
yarn e2e:syndesis-qe
```

Alternatively execute tests in Docker container with Xvfb
```bash
export SYNDESIS_UI_URL='https://<SYNDESIS_UI_URL>'
yarn e2e:xvfb
```


#### Execute subset of cucumber tests
Tests `*.feature` files can have something like java annotations.
In the cucumber docs it's called [tags](https://github.com/cucumber/cucumber/wiki/Tags).


Example of feature with tag `@narrative`
```gherkin
@narrative
Feature: First pass at login, homepage, connections, and integrations
  https://issues.jboss.org/browse/IPAAS-153
```https://yarnpkg.com/lang/en/docs/cli/run/

Can be run with command
```bash
# first -- tells yarn to pass these arguments to script
yarn e2e -- --cucumberOpts.tags="@narrative"
```

For more information about parameters see [yarn run docs](https://yarnpkg.com/lang/en/docs/cli/run/).
