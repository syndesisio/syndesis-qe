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
 inNamespace(cloud: 'openshift', prefix: 'e2e') {

    env = []
    env.add(containerEnvVar(key:'NAMESPACE_USE_EXISTING', value: "${KUBERNETES_NAMESPACE}"))
    env.add(containerEnvVar(key:'NAMESPACE_DESTROY_ENABLED', value: "false"))
    env.add(containerEnvVar(key:'NAMESPACE_CLEANUP_ENABLED', value: "false"))
    env.add(containerEnvVar(key:'ENV_INIT_ENABLED', value: "false"))
    env.add(containerEnvVar(key:'SYNDESIS_TEMPLATE_URL', value: 'https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-ephemeral-restricted.yml'))

    slave {
        withOpenshift {
            withYarn(envVar: env) {
                    inside {
                        stage 'Prepare Environment'
			          createEnvironment(
            				cloud: 'openshift',
            				environmentSetupScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/setup.sh",
            				environmentTeardownScriptUrl: "https://raw.githubusercontent.com/syndesisio/syndesis-system-tests/master/src/test/resources/teardown.sh",
            				waitForServiceList: ['syndesis-rest', 'syndesis-ui'],
           				waitTimeout: 600000L,
            				namespaceCleanupEnabled: false,
            				namespaceDestroyEnabled: false)

                        stage ('End to End Tests')
                        container(name: 'yarn') {
                            git url: 'https://github.com/syndesisio/syndesis-e2e-tests.git'
                            writeFile(file: 'e2e/data/users.json', text: "${users}")
                            try {
                                sh """
                                export SYNDESIS_UI_URL=https://syndesis-qe.b6ff.rh-idev.openshiftapps.com
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
