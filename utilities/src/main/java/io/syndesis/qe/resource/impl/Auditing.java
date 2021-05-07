package io.syndesis.qe.resource.impl;

import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Auditing implements Resource {

    String serverLogBeforeChange = null;

    @Override
    public void deploy() {
        serverLogBeforeChange = OpenShiftUtils.getPodLogs("syndesis-server");
        ResourceFactory.get(Syndesis.class).updateServerFeature("auditing", true);
    }

    @Override
    public void undeploy() {
        serverLogBeforeChange = OpenShiftUtils.getPodLogs("syndesis-server", 10000);
        ResourceFactory.get(Syndesis.class).updateServerFeature("auditing", false);
    }

    @Override
    public boolean isReady() {
        String newLogLines = StringUtils.difference(serverLogBeforeChange, OpenShiftUtils.getPodLogs("syndesis-server", 10000));
        return newLogLines.contains("Reloading using strategy: REFRESH");
    }

    @Override
    public boolean isDeployed() {
        return ResourceFactory.get(Syndesis.class).isServerFeatureEnabled("auditing");
    }
}
