package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    public Optional<String> getIntegrationId(String integrationName) {
        List<Integration> integrationsList = list();
        return integrationsList.stream().filter(i -> i.getName().contentEquals(integrationName)).findFirst().get().getId();
    }
}
