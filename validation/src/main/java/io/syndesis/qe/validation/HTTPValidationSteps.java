package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.impl.HTTPEndpoints;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.assertj.core.api.Assertions;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPValidationSteps {
    // Static to have this lpf shared between tests
    private static LocalPortForward localPortForward;

    @When("clear endpoint events")
    public void clear() {
        if (localPortForward != null) {
            OpenShiftUtils.terminateLocalPortForward(localPortForward);
        }
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName("endpoints");
        assertThat(pod.isPresent()).isTrue();
        localPortForward = OpenShiftUtils.createLocalPortForward(pod.get(), 8080, 28080);

        // Clear all events
        HTTPUtils.doDeleteRequest("http://localhost:28080/clearEvents");
    }

    @Then("verify that endpoint {string} was executed")
    public void verifyThatEndpointWasExecuted(String method) {
        verify(method, false);
    }

    @Then("verify that endpoint {string} was executed once")
    public void verifyThatEndpointWasExecutedOnce(String method) {
        verify(method, true);
    }

    private void verify(String method, boolean once) {
        // Let the integration running
        TestUtils.sleepIgnoreInterrupt(30000L);
        // Get new events
        HTTPResponse r = HTTPUtils.doGetRequest("http://localhost:28080/events");
        Map<Long, String> events = new Gson().fromJson(r.getBody(), Map.class);

        if (once) {
            assertThat(events).size().isEqualTo(1);
        } else {
            assertThat(events).size().isGreaterThanOrEqualTo(5);
        }
        for (String event : events.values()) {
            assertThat(method.equals(event));
        }
    }

    @Then("^verify that after (\\d+) seconds there (?:were|was) (\\d+) calls?$")
    public void verifyThatAfterSecondsWasCalls(int seconds, int calls) {
        clear();
        HTTPUtils.doGetRequest("http://localhost:28080/events"); // don't know why but first call returns only one event every time.
        TestUtils.sleepIgnoreInterrupt((long) seconds * 1000);
        HTTPResponse r = HTTPUtils.doGetRequest("http://localhost:28080/events");
        Map<Long, String> events = new Gson().fromJson(r.getBody(), Map.class);
        assertThat(events).size().isEqualTo(calls);
    }

    @When("send get request using {string} and {string} path")
    public void sendGetRequestUsingAndPath(String account, String path) {
        final Account a = AccountsDirectory.getInstance().get(account);
        HTTPUtils.doGetRequest(a.getProperty("baseUrl") + path);
    }

    @When("^configure keystore in (HTTP|HTTPS) integration dc$")
    public void configureKeystore(String protocol) {
        if ("HTTP".equals(protocol)) {
            return;
        }
        try {
            OpenShiftWaitUtils.waitFor(() -> !OpenShiftUtils.getInstance().deploymentConfigs().withLabel("syndesis.io/type", "integration").list()
                .getItems().isEmpty(), 300000L);
        } catch (TimeoutException | InterruptedException e) {
            fail("Unable to find integration deployment config after 5 minutes");
        } catch (Exception e) {
            // ignore
        }
        List<DeploymentConfig> integrationDcs =
            OpenShiftUtils.getInstance().deploymentConfigs().withLabel("syndesis.io/type", "integration").list().getItems();
        Assertions.assertThat(integrationDcs).as("There should be only one integration deployment config").hasSize(1);
        DeploymentConfig dc = integrationDcs.get(0);
        log.debug("Waiting until next integration state check interval");
        String serverLog = OpenShiftUtils.getPodLogs("syndesis-server");
        while (serverLog.equals(OpenShiftUtils.getPodLogs("syndesis-server"))) {
            TestUtils.sleepIgnoreInterrupt(5000L);
        }

        TestUtils.withRetry(() -> {
            try {
                //@formatter:off
                OpenShiftUtils.getInstance().deploymentConfigs().withName(dc.getMetadata().getName()).edit()
                    .editSpec()
                        .editTemplate()
                            .editSpec()
                                .addNewVolume()
                                    .withName("keystore")
                                    .withNewSecret()
                                        .withSecretName(HTTPEndpoints.KEYSTORE_SECRET_NAME)
                                    .endSecret()
                                .endVolume()
                                .editFirstContainer()
                                    .addNewVolumeMount()
                                        .withNewMountPath("/opt/jboss/")
                                        .withName("keystore")
                                    .endVolumeMount()
                                    .addToEnv(new EnvVar(
                                        "JAVA_OPTIONS",
                                        "-Djackson.deserialization.whitelist.packages=io.syndesis.common.model,io.atlasmap" +
                                        " -Djavax.net.ssl.trustStore=/opt/jboss/keystore.p12 -Djavax.net.ssl.trustStorePassword=tomcat -Djavax.net.ssl.trustStoreAlias=tomcat",
                                        null
                                    ))
                                .endContainer()
                            .endSpec()
                        .endTemplate()
                    .endSpec()
                .done();
                //@formatter:on
                // Just to be sure, delete the integration pod if it exists, so that the change in DC is picked up
                OpenShiftUtils.getInstance().deletePods("syndesis.io/type", "integration");
                return true;
            } catch (KubernetesClientException kce) {
                log.debug("Caught KubernetesClientException: ", kce);
                return false;
            }
        }, 3, 30000L, "Unable to edit deployment config");
    }
}
