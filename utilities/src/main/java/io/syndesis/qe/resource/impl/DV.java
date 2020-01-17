package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assumptions.assumeThat;

import io.syndesis.qe.Image;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.TestUtils;

/**
 * The whole DV stuff is controlled by the syndesis operator, this class is only for skipping DV tests if running prod build and the image is missing.
 */
public class DV implements Resource {
    @Override
    public void deploy() {
        if (TestUtils.isProdBuild()) {
            assumeThat(TestConfiguration.image(Image.DV)).as("No DV image was specified when running productized build").isNotNull();
        }
    }

    @Override
    public void undeploy() {
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
