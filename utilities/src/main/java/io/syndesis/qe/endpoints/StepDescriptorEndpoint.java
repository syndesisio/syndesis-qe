package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StepDescriptorEndpoint extends AbstractEndpoint<StepDescriptor> {

    public StepDescriptorEndpoint() {
        super(Action.class, null);
    }

    public StepDescriptor postParamsAction(String stepKind, DynamicActionMetadata metadata) {
        super.setEndpointName("/steps/" + stepKind + "/descriptor");
        log.debug("POST, destination : {}", getEndpointUrl());
        final Invocation.Builder invocation = createInvocation(null);
        JsonNode response;
        try {
            response = invocation.post(Entity.entity(metadata, MediaType.APPLICATION_JSON), JsonNode.class);
        } catch (BadRequestException ex) {
            log.error("Bad request, trying again in 15 seconds");
            TestUtils.sleepIgnoreInterrupt(15000L);
            response = invocation.post(Entity.entity(metadata, MediaType.APPLICATION_JSON), JsonNode.class);
        }
        return transformJsonNode(response, StepDescriptor.class);
    }
}
