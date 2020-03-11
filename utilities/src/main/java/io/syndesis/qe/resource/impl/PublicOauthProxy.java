package io.syndesis.qe.resource.impl;

import io.syndesis.qe.Addon;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublicOauthProxy implements Resource {
    private static final String POD_NAME = "syndesis-public-oauthproxy";
    public static final String PUBLIC_API_PROXY_ROUTE = "public-" + TestConfiguration.openShiftNamespace()
        + "." + TestConfiguration.openShiftRouteSuffix();

    @Override
    public void deploy() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("routeHostname", PUBLIC_API_PROXY_ROUTE);
        ResourceFactory.get(Syndesis.class).updateAddon(Addon.PUBLIC_API, true, properties);
    }

    @Override
    public void undeploy() {
        ResourceFactory.get(Syndesis.class).updateAddon(Addon.PUBLIC_API, false);
        OpenShiftWaitUtils.waitUntilPodIsDeleted(POD_NAME);
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("syndesis.io/component", POD_NAME));
    }
}
