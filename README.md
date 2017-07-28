# Syndesis E2E tests

Before test execution you should have Syndesis UI running, follow [the documentation](https://github.com/syndesisio/syndesis-ui#running).

Download test dependencies

```bash
yarn
yarn webdriver-manager update
```

#### Credentials

Create json config file `${PROJECT_ROOT}/test_config.json` with connection credentials
or specify `export SYNDESIS_TEST_CONFIG=/path/to/test-config.json` 
```json
{
  "users": {
    "camilla": {
      "username": "<GITHUB_USERNAME>",
      "password": "<GITHUB_PASSWORD>"
    }
  },
  "connection": {
    "Twitter Listener": {
      "accessToken": "YOUR_SECRET_DATA",
      "accessTokenSecret": "YOUR_SECRET_DATA",
      "consumerKey": "YOUR_SECRET_DATA",
      "consumerSecret": "YOUR_SECRET_DATA"
    },
    "QE Salesforce": {
      "clientId": "YOUR_SECRET_DATA",
      "clientSecret": "YOUR_SECRET_DATA",
      "password": "YOUR_SECRET_DATA",
      "userName": "YOUR_SECRET_DATA"
    },
    "Twitter" : "..."
  }
}
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
