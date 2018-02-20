package io.syndesis.qe.endpoints;

import io.syndesis.rest.v1.handler.activity.Activity;


public class ActivityIntegrationsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityIntegrationsEndpoint() {
        //TODO this part has not been done in engineering yet. to be updated later on:
        super(Activity.class, "/activity/integrations");
    }
}
