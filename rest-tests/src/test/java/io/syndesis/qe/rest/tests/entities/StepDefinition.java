package io.syndesis.qe.rest.tests.entities;

import java.util.List;
import java.util.Optional;

import io.atlasmap.v2.Field;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;
import lombok.Data;

/**
 * Feb 22, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Data
public class StepDefinition {

    private Step step;
    private Optional<ConnectorDescriptor> connectorDescriptor;
    private Optional<DataMapperDefinition> dataMapperDefinition;
    private Optional<List<Field>> inspectionResponseFields;

    public StepDefinition() {
    }

    public StepDefinition(Step step) {
        this.step = step;
        this.connectorDescriptor = Optional.empty();
    }

    public StepDefinition(Step step, ConnectorDescriptor connectorDescriptor) {
        this.step = step;
        this.connectorDescriptor = Optional.of(connectorDescriptor);
    }

    public StepDefinition(Step step, DataMapperDefinition dataMapperDefinition) {
        this.step = step;
        this.dataMapperDefinition = Optional.of(dataMapperDefinition);
        this.connectorDescriptor = Optional.empty();
    }
}
