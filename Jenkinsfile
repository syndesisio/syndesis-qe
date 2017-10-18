properties([
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
])

def namespace = 'syndesis-e2e-tests'

node {
  //wait for the test namespace to not exist to prevent conflicts
  timeout(600) {
    waitUntil {
      def r = sh script: "oc get project ${namespace}", returnStatus: true
      return (r == 1);
    }
  }

  inNamespace(cloud: 'openshift', name: "${namespace}") {
        
          stage 'Prepare test environment'
          createEnvironment(
                cloud: 'openshift',
                scriptEnvironmentVariables: ['SYNDESIS_RELEASED_IMAGES':'true', 'DEMO_DATA_ENABLED':'true'],
                environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
                environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
                waitForServiceList: ['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak'],
                waitTimeout: 600000L,
                namespaceCleanupEnabled: true,
                namespaceDestroyEnabled: true)

          slave {
            withOpenshift() {
              sh "oc policy add-role-to-user admin syndesisqe"
              def test_config = sh returnStdout: true, script: "oc get cm e2e-test-config -o jsonpath=\"{ .data.test_config }\" -n syndesis-ci"
              withYarn() {
                inside{
                    stage ('End to End Tests')
                    container(name: 'yarn') {
                      checkout scm
                      writeFile(file: './ui-tests-protractor/test_config.json', text: "${test_config}")
                      try {
                        ansiColor('xterm') {
                          sh """
                          export SYNDESIS_UI_URL=https://${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com
                          export SYNDESIS_E2E_RESTART=1
                          cd ui-tests-protractor
                          ./e2e-xvfb.sh
                          """
                        }
                      } catch(err) {
                        echo "E2E tests failed: ${err}"
                        currentBuild.result = 'FAILURE'
                      } finally {
                        archive includes: 'ui-tests-protractor/e2e/cucumber-reports/**/*'
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
