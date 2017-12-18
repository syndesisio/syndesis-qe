# Syndesis QE


### Structure

```bash
├── docs
├── manual
├── rest-tests
├── ui-tests
├── ui-tests-protractor
└── utilities
```

#### docs
On-going initiative to provide comprehensive guide to current code structure.

#### manual
BDD scenarios to be tested manually for now.

#### rest-tests
Java based tests that use Cucumber scenarios.
Test actions are executed directly to `syndesis-rest` backend.

#### ui-tests
Java based tests that use Selenide and Cucumber BDD scenarios.
Test actions are mainly UI driven with additional 3rd party validation like Salesforce, Twitter etc.

#### ui-tests-protractor (Deprecated)
Typescript based tests that use Protractor and Cucumber BDD scenarios.


### CI job

Circle CI job is configured to be executed on OpenShift Dedicated cluster, with configuration located in `.circleci/config.yml`

https://circleci.com/gh/syndesisio/syndesis-qe


### Scenarios
Test scenarios are provided in Gherkin language in a BDD fashion. Located in `./resources`
directory of `*-tests` module, e.g. [UI scenarios](https://github.com/syndesisio/syndesis-qe/tree/master/ui-tests/src/test/resources/features).

Every scenario is wrapped with appropriate tags to target specific execution on demand.

### Configuration
NOTE: Successful execution of tests requires fully configured credentials.
All the callback URLs, Oauth tokens, etc. for Salesforce and Twitter accounts.

Placed to the root of `syndesis-qe` directory.

test.properties
```
syndesis.config.openshift.url=https://192.168.64.2:8443
syndesis.config.openshift.token=<openshift-token>
syndesis.config.openshift.namespace=syndesis
syndesis.config.url.suffix=192.168.64.2.nip.io
syndesis.config.ui.url=https://syndesis.192.168.64.2.nip.io
syndesis.config.ui.username=developer
syndesis.config.ui.password=developer

```


credentials.json
```json
{
  "twitter_listen": {
    "service": "twitter",
    "properties": {
      "screenName": "************",
      "consumerKey": "*************************",
      "consumerSecret": "**************************************************",
      "accessToken": "**************************************************",
      "accessTokenSecret": "*********************************************"
    }
  },
  "twitter_talky": {
    "service": "twitter",
    "properties": {
      "screenName": "************",
      "consumerKey": "*************************",
      "consumerSecret": "**************************************************",
      "accessToken": "**************************************************",
      "accessTokenSecret": "*********************************************"
    }
  },
  "salesforce": {
    "service": "salesforce",
    "properties": {
      "instanceUrl": "https://developer.salesforce.com",
      "loginUrl": "https://login.salesforce.com",
      "clientId": "*************************************************************************************",
      "clientSecret": "*******************",
      "userName": "**********************",
      "password": "*********"
    }
  }
}
```

### Execution

For the test execution at least `syndesis-rest` modules are required in current SNAPSHOT version.

```
cd <syndesis-project-dir>
./syndesis/tools/bin/syndesis build --init --batch-mode --backend --flash
```

#### Test suite execution

There're three Maven profiles: `all, rest, ui` to target the specific test suite.

```
mvn clean test // default all profile
mvn clean test -P ui
mvn clean test -P rest
```

#### Cucumber tag execution
```
mvn clean test -P ui -Dcucumber.options="--tags @integrations-sf-db-test"
```
