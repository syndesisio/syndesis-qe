package io.syndesis.qe.rest.tests.steps.flow;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.qe.bdd.entities.DataMapperDefinition;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsActionsEndpoint;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 8, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public abstract class AbstractStep {
    public enum StepProperty {
        KIND,
        STEP_ID,
        STEP_NAME,
        CONNECTOR_ID,
        CONNECTION_ID,
        ACTION,
        PROPERTIES,
        EXTENSION
    }

    @Autowired
    @Getter
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    private Map<StepProperty, Object> properties = new HashMap<>();

    public void addProperty(StepProperty key, Object value) {
        properties.put(key, value);
    }

    public void createStep() {
        // Some steps do not have connector / connection
        Connector connector = properties.get(StepProperty.CONNECTOR_ID) == null ? null : connectorsEndpoint.get((String) properties.get(StepProperty.CONNECTOR_ID));
        Connection connection = properties.get(StepProperty.CONNECTION_ID) == null ? null : connectionsEndpoint.get((String) properties.get(StepProperty.CONNECTION_ID));
        Action action;
        // If the action is not String, then we already have action object, so just use it
        if (properties.get(StepProperty.ACTION) != null && !(properties.get(StepProperty.ACTION) instanceof String)) {
            action = (Action) properties.get(StepProperty.ACTION);
        } else {
            // It may not have an action
            action = properties.get(StepProperty.ACTION) == null ? null : findConnectorAction(connector, (String) properties.get(StepProperty.ACTION));
            if (action != null) {
                // Get the action with datashapes configured
                action = generateStepAction(action, getConnectorDescriptor(action, (Map) properties.get(StepProperty.PROPERTIES),
                        (String) properties.get(StepProperty.CONNECTION_ID)));
            }
        }

        final Step.Builder stepBuilder = new Step.Builder();
        stepBuilder.stepKind(properties.get(StepProperty.KIND) == null ? StepKind.endpoint : (StepKind) properties.get(StepProperty.KIND));
        stepBuilder.id(properties.get(StepProperty.STEP_ID) == null ? UUID.randomUUID().toString() : (String) properties.get(StepProperty.STEP_ID));
        stepBuilder.name(properties.get(StepProperty.STEP_NAME) == null ? UUID.randomUUID().toString() : (String) properties.get(StepProperty.STEP_NAME));
        if (connection != null) {
            stepBuilder.connection(connection);
        }
        if (action != null) {
            stepBuilder.action(action);
        }
        if (properties.get(StepProperty.PROPERTIES) != null) {
            stepBuilder.configuredProperties((Map) properties.get(StepProperty.PROPERTIES));
        }
        if (properties.get(StepProperty.EXTENSION) != null) {
            stepBuilder.extension((Extension) properties.get(StepProperty.EXTENSION));
        }
        if (properties.get(StepProperty.KIND) == StepKind.mapper) {
            steps.getStepDefinitions().add(new StepDefinition(stepBuilder.build(), new DataMapperDefinition()));
        } else {
            steps.getStepDefinitions().add(new StepDefinition(stepBuilder.build()));
        }
        properties.clear();
    }

    private Action findConnectorAction(Connector connector, String connectorPrefix) {
        Optional<ConnectorAction> action;
        if (connector == null) {
            fail("Incorrect parameter combination, connector was null, but action was provided");
        }

        action = connector.getActions()
                .stream()
                .filter(a -> a.getId().get().contains(connectorPrefix))
                .findFirst();

        if (!action.isPresent()) {
            action = connector.getActions()
                    .stream()
                    .filter(a -> a.getDescriptor().getCamelConnectorPrefix().contains(connectorPrefix))
                    .findFirst();
        }

        return action.get();
    }

    ConnectorDescriptor getConnectorDescriptor(Action action, Map configuredProperties, String connectionId) {
        ConnectionsActionsEndpoint conActEndpoint = new ConnectionsActionsEndpoint(connectionId);
        return conActEndpoint.postParamsAction(action.getId().get(), configuredProperties);
    }

    //Small hack -> the Action doesn't provide setters for input/output data shape
    Action generateStepAction(Action action, ActionDescriptor connectorDescriptor) {
        ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        Action ts = null;
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(action));
            JSONObject inputDataType = new JSONObject(mapper.writeValueAsString(connectorDescriptor.getInputDataShape().get()));
            JSONObject outputDataType = new JSONObject(mapper.writeValueAsString(connectorDescriptor.getOutputDataShape().get()));
            JSONArray propertyDefinitionSteps = new JSONArray(mapper.writeValueAsString(connectorDescriptor.getPropertyDefinitionSteps()));

            json.getJSONObject("descriptor").put("inputDataShape", inputDataType);
            json.getJSONObject("descriptor").put("outputDataShape", outputDataType);
            json.getJSONObject("descriptor").put("propertyDefinitionSteps", propertyDefinitionSteps);

            ts = JsonUtils.reader().forType(Action.class).readValue(json.toString());
        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return ts;
    }

    /**
     * Sets the custom datashape to the action object.
     * @param action action
     * @param connectorDescriptor action's connector descriptor to fill other values
     * @param direction "in" for InputDataShape, "out" for OutputDataShape
     * @param kind {@link DataShapeKinds} value
     * @param datashape Datashape specification
     * @return action object with datashapes
     */
    Action withCustomDatashape(Action action, ConnectorDescriptor connectorDescriptor, String direction, DataShapeKinds kind, String datashape) {
        // This will set datashapes and property definitions from the connectorDescriptor
        Action a = generateStepAction(action, connectorDescriptor);
        ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        try {
            JSONObject json = new JSONObject(mapper.writeValueAsString(a));
            DataShape ds = new DataShape.Builder()
                    .name(kind.toString())
                    .description(kind.toString())
                    .kind(kind)
                    .specification(datashape)
                    .build();

            json.getJSONObject("descriptor").put("in".equals(direction) ? "inputDataShape" : "outputDataShape", new JSONObject(mapper.writeValueAsString(ds)));

            a = JsonUtils.reader().forType(Action.class).readValue(json.toString());
        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return a;
    }


}
