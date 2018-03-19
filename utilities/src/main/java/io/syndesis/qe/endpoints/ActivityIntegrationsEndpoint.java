package io.syndesis.qe.endpoints;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;

public class ActivityIntegrationsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityIntegrationsEndpoint() {
        super(Activity.class, "/activity/integrations");
    }
}
