package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.CamelK;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assumptions;

import java.util.List;
import java.util.stream.Collectors;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestHooks {
    @Before("@prod")
    public void skipProdForNightly() {
        // Skip prod tests when not running with productized build
        Assumptions.assumeThat(TestConfiguration.syndesisVersion()).contains("redhat");
    }

    @Before("@osd")
    public void skipIfNotOSD() {
        // Skip test if environment is not OSD (with SSO)
        Assumptions.assumeThat(OpenShiftUtils.isOSD()).isTrue();
    }

    @Before("@ocp3-only")
    public void skipIfNotOCP3() {
        // Skip test if environment is not OSD (with SSO)
        Assumptions.assumeThat(OpenShiftUtils.isOpenshift3()).isTrue();
    }

    @AfterStep
    public void getLogs(Scenario scenario) {
        if (scenario.isFailed()) {
            TestUtils.printPods(scenario);
            log.warn("Scenario {} failed, saving integration logs to scenario", scenario.getName());
            // There can be multiple integration pods for one test
            List<Pod> integrationPods = OpenShiftUtils.getInstance().pods().list().getItems().stream().filter(
                p -> p.getMetadata().getName().startsWith("i-")
                    && !p.getMetadata().getName().contains("deploy")
                    && !p.getMetadata().getName().contains("build")
            ).collect(Collectors.toList());
            for (Pod integrationPod : integrationPods) {
                try {
                    scenario.attach(OpenShiftUtils.getInstance().getPodLog(integrationPod).getBytes(), "text/plain",
                        String.format("Integration %s log", integrationPod.getMetadata().getName()));
                } catch (KubernetesClientException ex) {
                    //when the build failed, the integration pod is not ready (`ImagePullBackOff`) In that case, the pod doesn't contain log. That
                    // causes that OpenShiftUtils has thrown KubernetesClientException
                }
            }
            log.info("Adding all failed build to the log");
            List<Pod> failedBuilds = OpenShiftUtils.getInstance().pods().list().getItems().stream().filter(
                p -> p.getMetadata().getName().contains("build")
                    && p.getStatus().getContainerStatuses().stream().anyMatch(c -> c.getState().getTerminated().getReason().equals("Error"))
            ).collect(Collectors.toList());
            for (Pod failedBuild : failedBuilds) {
                scenario.attach(String.format("%s\n\n%s", failedBuild.getMetadata().getName(),
                    OpenShiftUtils.getInstance().getPodLog(failedBuild)).getBytes(), "text/plain",
                    "Log of failed build " + failedBuild.getMetadata().getName());
            }
        }
    }

    @After("@camel-k")
    public void cleanCamelK() {
        if (!"camelk".equals(TestConfiguration.syndesisRuntime())) {
            log.info("Changing Syndesis runtime back to springboot");
            ResourceFactory.destroy(CamelK.class);
        }
    }
}
