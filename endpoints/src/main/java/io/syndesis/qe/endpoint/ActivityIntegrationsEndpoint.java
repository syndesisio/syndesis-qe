package io.syndesis.qe.endpoint;

import io.syndesis.server.endpoint.v1.handler.activity.Activity;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.GenericType;

import java.util.List;

@Component
public class ActivityIntegrationsEndpoint extends AbstractEndpoint<Activity> {

    public ActivityIntegrationsEndpoint() {
        super(Activity.class, "/activity/integrations");
    }

    // we need to override Activity fetching because it isn't server as ListResult
    @Override
    public List<Activity> list(String id) {
        return this.createInvocation(id).get(new GenericType<List<Activity>>() { });
    }
}
