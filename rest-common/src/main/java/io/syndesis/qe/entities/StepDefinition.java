package io.syndesis.qe.entities;

import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Step;

import java.util.List;

import io.atlasmap.v2.Field;
import lombok.Data;

/**
 * Feb 22, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Data
public class StepDefinition {

    private Step step;
    private ConnectorDescriptor connectorDescriptor;
    private DataMapperDefinition dataMapperDefinition;
    private List<Field> inspectionResponseFields;

    public StepDefinition() {
    }

    public StepDefinition(Step step) {
        this.step = step;
    }

    public StepDefinition(Step step, ConnectorDescriptor connectorDescriptor) {
        this.step = step;
        this.connectorDescriptor = connectorDescriptor;
    }

    public StepDefinition(Step step, DataMapperDefinition dataMapperDefinition) {
        this.step = step;
        this.dataMapperDefinition = dataMapperDefinition;
    }
}
