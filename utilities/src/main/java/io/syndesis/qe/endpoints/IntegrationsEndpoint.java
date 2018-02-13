package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.model.integration.Integration;

/**
 * Integrations client endpoint.
 *
 * @author jknetl
 */
@Component
public class IntegrationsEndpoint extends AbstractEndpoint<Integration> {

    public IntegrationsEndpoint() {
        super(Integration.class, "/integrations");
    }
}
