package io.syndesis.qe.bdd;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.util.Json;
import io.syndesis.qe.endpoints.ConnectionsActionsEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 8, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public abstract class AbstractStep {

    public ConnectorDescriptor getConnectorDescriptor(Action action, Map properties, String connectionId) {

        ConnectionsActionsEndpoint conActEndpoint = new ConnectionsActionsEndpoint(connectionId);
        return conActEndpoint.postParamsAction(action.getId().get(), properties);
    }

    //Small hack -> the Action doesn't provide setters for input/output data shape
    public Action generateStepAction(Action action, ConnectorDescriptor connectorDescriptor) {
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

            ts = Json.reader().forType(Action.class).readValue(json.toString());
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
    public Action withCustomDatashape(Action action, ConnectorDescriptor connectorDescriptor, String direction, DataShapeKinds kind, String datashape) {
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

            a = Json.reader().forType(Action.class).readValue(json.toString());
        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return a;
    }
}
