@Library('syndesis-pipeline-library@master')

def username = System.getenv().get('GITHUB_USERNAME')
def password = System.getenv().get('GITHUB_PASSWORD')
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
cube.namespace().withCloud('openshift').withPrefix('e2e').inside {

    env = []
    env.add(containerEnvVar(key:'NAMESPACE_USE_EXISTING', value: "${KUBERNETES_NAMESPACE}"))
    env.add(containerEnvVar(key:'NAMESPACE_DESTROY_ENABLED', value: "false"))
    env.add(containerEnvVar(key:'NAMESPACE_CLEANUP_ENABLED', value: "false"))
    env.add(containerEnvVar(key:'ENV_INIT_ENABLED', value: "false"))

    slave {
        withOpenshift {
            withYarn(envVar: env) {
              inside {
                stage 'Prepare Environment'
                cube.environment()
                  .withName("${KUBERNETES_NAMESPACE}")
                  .withSetupScriptUrl('https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh')
                  .withTeardownScriptUrl('https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh')
                  .withServicesToWait(['syndesis-rest', 'syndesis-ui', 'syndesis-keycloak', 'syndesis-verifier'])
                  .withWaitTimeout(1200000L)
                  .withNamespaceDestroyEnabled(false)
                  .withNamespaceCleanupEnabled(false)
                  .create()

                  stage 'End to End Tests'
                  container(name: 'yarn') {
                    git url: 'https://github.com/dsimansk/syndesis-ui.git', branch: 'e2e-fixes'
                    writeFile(file: 'e2e/data/users.json', text: "${users}")
                    try {
                      sh """
                      export SYNDESIS_UI_URL=https://${KUBERNETES_NAMESPACE}.b6ff.rh-idev.openshiftapps.com
                      ./e2e-xvfb.sh
                      """
                    } catch(err) {
                      echo "E2E tests failed: ${err}"
                      } finally {
                      archive: 'e2e/cucumber-reporsts/*'
                    }
                  }

              }
            }
          }
    }
}
