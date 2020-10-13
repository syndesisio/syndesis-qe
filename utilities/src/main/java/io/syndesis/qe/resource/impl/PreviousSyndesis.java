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
        super.installCluster();
        super.grantPermissions();
        super.deployOperator();
        deploySyndesisViaOperator();
    }

    @Override
    public void deploySyndesisViaOperator() {
        // currently the deployment methods are the same, but keep this method here in case of future diverge of deployment methods
        super.deploySyndesisViaOperator();
    }
}
