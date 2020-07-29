package io.syndesis.qe.endpoint;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;

public class ActivityLogsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityLogsEndpoint() {
        //TODO this part has not been done in engineering yet. to be updated later on:
        super(Activity.class, "/activity/logs");
    }
}
