package io.syndesis.qe.hooks;

import static org.junit.Assume.assumeTrue;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.CamelK;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import java.util.List;
import java.util.stream.Collectors;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestHooks {
    @Before("@prod")
    public void skipProdForNightly() {
        // Skip prod tests when not running with productized build
        assumeTrue(TestConfiguration.syndesisVersion().contains("redhat"));
    }

    @After
    public void getLogs(Scenario scenario) {
        if (scenario.isFailed()) {
            TestUtils.printPods();
            log.warn("Scenario {} failed, saving integration logs to scenario", scenario.getName());
            // There can be multiple integration pods for one test
            List<Pod> integrationPods = OpenShiftUtils.getInstance().pods().list().getItems().stream().filter(
                p -> p.getMetadata().getName().startsWith("i-")
                    && !p.getMetadata().getName().contains("deploy")
                    && !p.getMetadata().getName().contains("build")
            ).collect(Collectors.toList());
            for (Pod integrationPod : integrationPods) {
                try {
                    scenario.embed(String.format("%s\n\n%s", integrationPod.getMetadata().getName(),
                        OpenShiftUtils.getInstance().getPodLog(integrationPod)).getBytes(), "text/plain");
                } catch (KubernetesClientException ex) {
                    //when the build failed, the integration pod is not ready (`ImagePullBackOff`) In that case, the pod doesn't contain log. That
                    // causes that OpenShiftUtils has thrown KubernetesClientException
                }
            }
            log.warn("Adding all failed build to the log");
            List<Pod> failedBuilds = OpenShiftUtils.getInstance().pods().list().getItems().stream().filter(
                p -> p.getMetadata().getName().contains("build")
                    && p.getStatus().getContainerStatuses().stream().anyMatch(c -> c.getState().getTerminated().getReason().equals("Error"))
            ).collect(Collectors.toList());
            for (Pod failedBuild : failedBuilds) {
                log.warn("Build with name {} failed. Failed pod log:", failedBuild.getMetadata().getName());
                log.error(OpenShiftUtils.getInstance().getPodLog(failedBuild));
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
