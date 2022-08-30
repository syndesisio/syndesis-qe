package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreviousSyndesis extends Syndesis {
    @Override
    public void deploy() {
        log.info("Deploying previous version of Syndesis");
        log.info("  Cluster:   " + TestConfiguration.openShiftUrl());
        log.info("  Namespace: " + TestConfiguration.openShiftNamespace());
        super.createPullSecret();
        super.pullOperatorImage();
        super.grantPermissions();
        super.installCluster();
        super.deployOperator();
        deploySyndesisViaOperator();
        super.workaround411();
        super.workaround411();
    }
}
