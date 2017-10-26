# syndesis-rest integration tests

This repository contains integration tests for syndesis-rest module.

## Running tests

You may run the tests against a syndesis instance deployed on OpenShift as follows:

```bash
mvn clean install -Dcredentials.file=<PATH_TO_CREDENTIALS_FILE>
```

## System properties

Several properties may influence coarse of the tests. Mandatory properties are shown in bold.

* **credentials.file**
    * JSON file with third party services credentials (see example below)
* **openshift.url**
    * token for accessing Openshift
* **openshift.token**
    * openshift master url
* **versions.file**
    * Properties file which contains expected dependencies versions of syndesis generated integrations

### Credentials

These tests require access to third party services such as Twitter, and Salesforce. Also necessary are openshift and syndesis information. You will need at least:

* a Salesforce account
* two Twitter accounts

You should configure credentials for these services in JSON file with following syntax (you need to replace *** with valid values):

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
  "openshift":	{
    "service":	"openshift",
    "properties":	{
      "instanceUrl":	"***************************",
      "userUID":	""
    }
  },
  "syndesis":	{
    "service":	"syndesis",
    "properties":	{
      "instanceUrl":	"**********************************",
      "login":	"****",
      "password":	"****"
    }
  }
}
```
