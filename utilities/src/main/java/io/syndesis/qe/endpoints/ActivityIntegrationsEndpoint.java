package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;

@Component
public class ActivityIntegrationsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityIntegrationsEndpoint() {
        super(Activity.class, "/activity/integrations");
    }
}
