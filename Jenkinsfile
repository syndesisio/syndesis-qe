def username = System.getenv().get('GITHUB_USERNAME')
def password = System.getenv().get('GITHUB_PASSWORD')
def namespace = System.getenv().get('E2E_NAMESPACE')
def clientId = System.getenv().get('GITHUB_E2E_OAUTH_CLIENT_ID')
def clientSecret = System.getenv().get('GITHUB_E2E_OAUTH_CLIENT_SECRET')


def users = """
{
  \"users\": {
    \"camilla\": {
      \"username\": \"${username}\",
      \"password\": \"${password}\"
    }
  }
}
"""

node {
  inNamespace(cloud: 'openshift', name: "${namespace}") {

          slave {
            withOpenshift( envVars: [
                    containerEnvVar(key:'SYNDESIS_E2E_SECRET', value:'true'),
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_ID', value: "${clientId}"),
                    containerEnvVar(key:'GITHUB_OAUTH_CLIENT_SECRET', value: "${clientSecret}")]
            ) {
              inside {
              stage 'Prepare test environment'
              createEnvironment(
                    cloud: 'openshift',
                    scriptEnvironmentVariables: ['SYNDESIS_E2E_SECRET':'true','GITHUB_E2E_OAUTH_CLIENT_ID':"${clientId}",'GITHUB_E2E_OAUTH_CLIENT_SECRET':"${clientSecret}"],
                    environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
                    environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
                    waitForServiceList: ['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak'],
                    waitTimeout: 600000L,
                    namespaceCleanupEnabled: true,
                    namespaceDestroyEnabled: true)
              }

              def test_config = sh returnStdout: true, script: "oc get cm e2e-test-config -o jsonpath=\"{ .data.test_config }\" -n syndesis-ci"
              withYarn() {
                inside{
                    stage ('End to End Tests')
                    container(name: 'yarn') {
                      checkout scm
                      writeFile(file: 'e2e/data/users.json', text: "${users}")
                      writeFile(file: 'test_config.json', text: "${test_config}")
                      try {
                        sh """
                        export SYNDESIS_UI_URL=https://${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com
                        ./e2e-xvfb.sh
                        """
                      } catch(err) {
                        echo "E2E tests failed: ${err}"
                        currentBuild.result = 'FAILURE'
                      } finally {
                        archive includes: 'e2e/cucumber-reports/*'
                      }
                    }

                }
              }
              stage "Cleanup environment"
              echo "Remove oauthclient"
              sh "oc delete oauthclient ${namespace}"
            }
          }
  }
}
