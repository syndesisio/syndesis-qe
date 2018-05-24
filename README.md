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


### Creating a Pull Request

When you create a PR on GitHub a new Jenkins job is scheduled. This job runs on Fuse QE Jenkins instance and runs a basic subset of tests (annotated with @smoke tag).

If you want to run a different subset of tests, you can use `//test: @mytag1, @mytag2` in your PR description to trigger specific tests annotated by given tags.

If you don't want to run the job for your PR at all (for example when you are changing small things in README file), you can use `//skip-ci` in your PR description.

When the PR job fails because of test failures and you believe that you didn't cause the error, you can try to trigger the job once again. For this just comment `retest this please` in the PR and a new build will be triggered in few minutes.

Please remember that each time you push something new (or amend something old) in the PR, a new build is triggered automatically, so you don't need to do anything else to get your PR rebuilt. 


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
  },
  "s3": {
    "service": "s3",
    "properties": {
      "region": "********",
      "accessKey": "********************",
      "secretKey": "*****************************************"
    }
  },
  "ftp": {
    "service": "ftp",
    "properties": {
      "binary": "Yes",
      "connectTimeout": "10000",
      "disconnect": "No",
      "host": "ftpd",
      "maximumReconnectAttempts": "3",
      "passiveMode": "Yes",
      "password": "",
      "port": "2121",
      "reconnectDelay": "1000",
      "timeout": "30000",
      "username": "anonymous"
    }
  },
  "QE Dropbox": {
  	  "service": "dropbox",
  	  "properties": {
  	  	"accessToken": "**************",
  		"clientIdentifier": "**************"
  	  }
    },
    "amq": {
        "service": "amq",
        "properties": {
          "brokerUrl": "tcp://broker-amq:61616",
          "username": "amq",
          "password": "topSecret",
          "clientId": "zzz"
        }
      },
   "amqp": {
        "service": "amqp",
        "properties": {
          "connectionUri": "amqp://broker-amq:5672",
          "username": "amq",
          "password": "topSecret",
          "clientID": "zzz",
          "skipCertificateCheck":"Disable",
          "brokerCertificate":"",
          "clientCertificate":""
        }
   }
}
```
for ftp connection credentials:
All values are just examples / proposals. Need to be updated in accordance with 

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
