package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreviousSyndesis extends Syndesis {
    @Override
    public void deploy() {
        log.info("Deploying previous version of Syndesis");
        log.info("  Cluster:   " + TestConfiguration.openShiftUrl());
        log.info("  Namespace: " + TestConfiguration.openShiftNamespace());
        super.createPullSecret();
        super.deployCrd();
        super.pullOperatorImage();
        super.grantPermissions();
        super.deployOperator();
        deploySyndesisViaOperator();
    }

    private void deploySyndesisViaOperator() {
        log.info("Deploying syndesis resource from " + super.getCrUrl());
        try (InputStream is = new URL(super.getCrUrl()).openStream()) {
            JSONObject crJson = new JSONObject(getSyndesisCrClient().load(is));
            JSONObject integration = crJson.getJSONObject("spec").getJSONObject("integration");
            integration.put("limit", 5);
            integration.put("stateCheckInterval", TestConfiguration.stateCheckInterval());

            super.addMavenRepo(crJson.getJSONObject("spec"));

            // set correct image stream namespace
            crJson.getJSONObject("spec").put("imageStreamNamespace", TestConfiguration.openShiftNamespace());

            // set the route
            crJson.getJSONObject("spec").put("routeHostname", TestConfiguration.syndesisUrl() != null
                ? StringUtils.substringAfter(TestConfiguration.syndesisUrl(), "https://")
                : TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix());

            getSyndesisCrClient().create(TestConfiguration.openShiftNamespace(), crJson.toMap());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load operator syndesis template", ex);
        }
    }
}
