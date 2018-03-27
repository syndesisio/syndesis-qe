package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import java.util.List;

@Component
public class ActivityIntegrationsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityIntegrationsEndpoint() {
        super(Activity.class, "/activity/integrations");
    }

    // we need to override Activity fetching because it isn't server as ListResult
    public List<Activity> list(String id) {
        final Invocation.Builder invocation = this.createInvocation(id);

        final List<Activity> activities = invocation.get(new GenericType<List<Activity>>() {});
        return  activities;
    }
}
