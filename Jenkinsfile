def username = System.getenv().get('GITHUB_USERNAME')
def password = System.getenv().get('GITHUB_PASSWORD')
def openshiftMaster = System.getenv().get('OPENSHIFT_MASTER')

echo "OPENSHIFT_MASTER:${openshiftMaster}"
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
inNamespace(cloud:'openshift', prefix: 'e2e') {

  node {
    stage 'Prepare test environment'
    createEnvironment(
          environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
          environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
          waitForServiceList: ['syndesis-rest', 'syndesis-ui'],
          waitTimeout: 600000L,
          namespaceCleanupEnabled: false,
     			namespaceDestroyEnabled: false)
  }

  slave {
    withOpenshift {
      withYarn(envVar: env) {
        inside{

          stage ('End to End Tests')
            container(name: 'yarn') {
              checkout scm
              writeFile(file: 'e2e/data/users.json', text: "${users}")
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
    }
  }
}
