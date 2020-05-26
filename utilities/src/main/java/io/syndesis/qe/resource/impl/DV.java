package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assumptions.assumeThat;

import io.syndesis.qe.Addon;
import io.syndesis.qe.Image;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

/**
 * The whole DV stuff is controlled by the syndesis operator, this class is only for skipping DV tests if running prod build and the image is missing.
 */
public class DV implements Resource {
    private static final String POD_NAME = "syndesis-dv";

    @Override
    public void deploy() {
        if (TestUtils.isProdBuild()) {
            assumeThat(TestConfiguration.image(Image.DV)).as("No DV image was specified when running productized build").isNotNull();
        }
        ResourceFactory.get(Syndesis.class).updateAddon(Addon.DV, true);
        OpenShiftWaitUtils.waitUntilPodIsRunning(POD_NAME);
    }

    @Override
    public void undeploy() {
        ResourceFactory.get(Syndesis.class).updateAddon(Addon.DV, false);
        OpenShiftWaitUtils.waitUntilPodIsDeleted(POD_NAME);
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("syndesis.io/component", POD_NAME));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.getAnyPod("syndesis.io/component", POD_NAME).isPresent();
    }
}
