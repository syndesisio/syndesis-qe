# Syndesis QE


### Structure

```bash
├── docs
├── rest-tests
├── ui-tests
├── ui-tests-protractor
└── utilities
```

#### docs
On-going initiative to provide comprehensive guide to current code structure.

#### rest-tests
Java based tests that use Cucumber scenarios.
Test actions are executed directly to `syndesis-rest` backend.

#### ui-tests
Java based tests that use Selenide and Cucumber BDD scenarios.
Test actions are mainly UI driven with additional 3rd party validation like Salesforce, Twitter etc.

#### ui-tests-protractor (Deprecated)
Typescript based tests that use Protractor and Cucumber BDD scenarios.



### Prepare syndesis instance
Updated: 16.8.2018

#### Prerequisites:
Installed minishift
Following lines in /etc/hosts file, insert your minishift ip:

	`${minishift_ip} syndesis.my-minishift.syndesis.io`
	`${minishift_ip} todo-syndesis.my-minishift.syndesis.io`

Due to --route option when installing syndesis and updated /etc/hosts file we don't
have to update all third party applications and their callbacks for every minishift/openshift IP.

For more information ask mcada@redhat.com or avano@redhat.com or tplevko@redhat.com

#### Enable the admin user on Minishift. Needs to be done only once and then restart minishift
`minishift addons enable admin-user`

#### Create a minishift instance
`minishift start --memory 4192`

#### Switch to admin
`oc login -u system:admin`

#### Register CRD and grant permissions to "developer"
`syndesis --setup --grant developer --cluster`

#### Switch to account developer
`oc login -u developer`

#### Clone Syndesis
`git clone https://github.com/syndesisio/syndesis.git`

#### Install Syndesis
`syndesis/tools/bin/syndesis install --local -y -p syndesis --route syndesis.my-minishift.syndesis.io --test-support`



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


#### Example of test.properties to run on minishift
File `test.properties` should be located in root of syndesis-qe folder.
Working example can be found in jenkins nightly build run logs.

For more information ask mcada@redhat.com or avano@redhat.com or tplevko@redhat.com

test.properties
```
syndesis.config.openshift.url=https://192.168.64.2:8443
syndesis.config.openshift.namespace=syndesis
syndesis.config.openshift.route.suffix=my-minishift.syndesis.io

syndesis.dballocator.url=http://dballocator.mw.lab.eng.bos.redhat.com:8080

#timeout in seconds, if not set default is 300
syndesis.config.timeout=300

#delay in seconds, if not set default is 1
jenkins.delay=7

syndesis.config.ui.url=https://syndesis.my-minishift.syndesis.io
syndesis.config.ui.username=developer
syndesis.config.ui.password=developer
syndesis.config.ui.browser=firefox
```

#### Example of credentials.json
File `credentials.json` should be located in root of syndesis-qe folder.
Working example on demand.

For more information ask mcada@redhat.com or avano@redhat.com or tplevko@redhat.com

credentials.json
```json
{
  "twitter_listener": {
    "service": "twitter",
    "properties": {
      "screenName": "****",
      "accessToken": "****",
      "accessTokenSecret": "****",
      "consumerKey": "****",
      "consumerSecret": "*****",
      "login": "****",
      "password": "****"
    }
  },
  "twitter_talky": {
    "service": "twitter",
    "properties": {
      "screenName": "****",
      "consumerKey": "****",
      "consumerSecret": "****",
      "accessToken": "****",
      "accessTokenSecret": "****",
      "login": "****",
      "password": "****"
    }
  },
  "salesforce": {
    "service": "salesforce",
    "properties": {
      "instanceUrl": "https://developer.salesforce.com",
      "loginUrl": "https://login.salesforce.com",
      "clientId": "****",
      "clientSecret": "****",
      "userName": "****",
      "password": "****"
    }
  },
  "QE Salesforce": {
    "service": "salesforce",
    "properties": {
      "instanceUrl": "https://developer.salesforce.com",
      "loginUrl": "https://login.salesforce.com",
      "clientId": "****",
      "clientSecret": "****",
      "userName": "****",
      "password": "****"
    }
  },
  "Twitter Listener": {
    "service": "twitter",
    "properties": {
      "screenName": "****",
      "accessToken": "****",
      "accessTokenSecret": "****",
      "consumerKey": "****",
      "consumerSecret": "****"
    }
  },
  "s3": {
    "service": "s3",
    "properties": {
      "region": "****",
      "accessKey": "****",
      "secretKey": "****"
    }
  },
  "syndesis": {
    "service": "syndesis",
    "properties": {
      "instanceUrl": "****",
      "login": "****",
      "password": "****"
    }
  },
  "QE Dropbox": {
    "service": "dropbox",
    "properties": {
      "accessToken": "****",
      "clientIdentifier": "****"
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
      "password": "****",
      "port": "****",
      "reconnectDelay": "1000",
      "timeout": "30000",
      "username": "****"
    }
  },
  "QE Slack": {
    "service": "slack",
    "properties": {
      "webhookUrl": "****",
      "Token": "****"
    }
  },
  "QE Google Mail": {
    "service": "Google Mail",
    "properties": {
      "clientId": "****",
      "clientSecret": "****",
      "applicationName": "****",
      "refreshToken": "****",
      "userId": "****",
      "email": "****",
      "password": "****"
    }
  },
  "telegram": {
    "service": "telegram",
    "properties": {
      "authorizationToken": "****"
    }
  },
  "GitHub": {
    "service": "GitHub",
    "properties": {
      "PersonalAccessToken": "****"
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

Working with extensions requires syndesis-extensions submodule compiled
```
cd syndesis-extensions
mvn clean install
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
