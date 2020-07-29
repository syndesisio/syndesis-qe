package io.syndesis.qe.endpoint;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StepDescriptorEndpoint extends AbstractEndpoint<StepDescriptor> {

    public StepDescriptorEndpoint() {
        super(Action.class, null);
    }

    public StepDescriptor getStepDescriptor(String stepKind, DataShape in, DataShape out) {
        super.setEndpointName("/steps/" + stepKind + "/descriptor");
        log.debug("POST, destination : {}", getEndpointUrl());
        final Invocation.Builder invocation = createInvocation(null);
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder().inputShape(in).outputShape(out).build();
        JsonNode response;
        try {
            response = invocation.post(Entity.entity(metadata, MediaType.APPLICATION_JSON), JsonNode.class);
        } catch (BadRequestException ex) {
            log.error("Bad request, trying again in 15 seconds");
            try {
                Thread.sleep(15000L);
            } catch (InterruptedException ignore) {
                // ignore
            }
            response = invocation.post(Entity.entity(metadata, MediaType.APPLICATION_JSON), JsonNode.class);
        }
        return transformJsonNode(response, StepDescriptor.class);
    }
}
