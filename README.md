# Syndesis E2E tests

### CI job

To check the report of automated tests, you need to have access to our Jenkins instance inside Dedicated cluster.

https://jenkins-syndesis-ci.b6ff.rh-idev.openshiftapps.com/job/syndesis-e2e-tests/

Currently the job are executed manually on demand and with every PR to this repository.



### Execute locally

Before test execution you should have Syndesis UI running, follow [the documentation](https://github.com/syndesisio/syndesis-ui#running).

Test are located in `ui-tests-protractor`

Download test dependencies

```bash
cd ui-tests-protractor
yarn
yarn webdriver-manager update
```

#### Credentials

Create json config file `${PROJECT_ROOT}/test_config.json` with connection credentials
or specify `export SYNDESIS_TEST_CONFIG=/path/to/test_config.json` 
```json
{
  "users": {
    "camilla": {
      "username": "<GITHUB_USERNAME>",
      "password": "<GITHUB_PASSWORD>",
      "userDetails": {
        "email": "camilla@gmail.com",
        "firstName": "Camilla",
        "lastName": "Syndesio"
      }
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
  },
  "settings": {
    "Twitter": {
      "clientId": "aaaaaaj to je ID",
      "clientSecret": "aaa to je seeecret"
    }
  }
}
```

#### Test environment configuration
Define env variable that points to your Syndesis UI web console

```bash
export SYNDESIS_UI_URL='https://<SYNDESIS_UI_URL>'

# default browser is chrome, to run with firefox:
export BROWSER=Firefox

# optionally restart browser after each feature with
export SYNDESIS_E2E_RESTART=1

yarn e2e:syndesis-qe
```

For executing tests on local minishift instance: 

```bash
export SYNDESIS_UI_URL=https://syndesis.$(minishift ip).nip.io

yarn e2e
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
