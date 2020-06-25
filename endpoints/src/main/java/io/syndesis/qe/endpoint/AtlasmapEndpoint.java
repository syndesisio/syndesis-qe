package io.syndesis.qe.endpoint;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.qe.endpoint.client.EndpointClient;

import org.springframework.stereotype.Component;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 27, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Component
public class AtlasmapEndpoint extends AbstractEndpoint<JsonInspectionResponse> {

    public AtlasmapEndpoint() {
        super(JsonInspectionRequest.class, "/atlas");
        client = EndpointClient.getWrappedClient();
    }

    /**
     * Sends the JSON inspection request for given specification and datakind and returns the JSON inspection response.
     * @param specification datashape specification
     * @param dsKind datashape kind
     * @return JSON inspection response
     */
    public JsonInspectionResponse inspectJson(String specification, DataShapeKinds dsKind) {
        final Invocation.Builder invocation = this.createInvocation("json/inspect");
        return invocation.post(
                Entity.entity((JsonInspectionRequest) generateInspectionRequest(specification, dsKind), MediaType.APPLICATION_JSON_TYPE),
                JsonInspectionResponse.class
        );
    }

    /**
     * Sends the XML inspection request for given specification and datakind and returns the XML inspection response.
     * @param specification datashape specification
     * @param dsKind datashape kind
     * @return XML inspection response
     */
    public XmlInspectionResponse inspectXml(String specification, DataShapeKinds dsKind) {
        final Invocation.Builder invocation = this.createInvocation("xml/inspect");
        return invocation.post(
                Entity.entity((XmlInspectionRequest) generateInspectionRequest(specification, dsKind), MediaType.APPLICATION_JSON_TYPE),
                XmlInspectionResponse.class
        );
    }

    /**
     * Creates InspectionRequest object out of specified datashape specification for given kind.
     *
     * @param specification specification
     * @param dsKind datashape kind
     * @return inspection request object {@link XmlInspectionRequest} for XML Datashapes {@link JsonInspectionRequest} for JSON Datashapes
     */
    private Object generateInspectionRequest(String specification, DataShapeKinds dsKind) {
        log.debug(specification);
        switch (dsKind) {
            case XML_SCHEMA:
            case XML_INSTANCE: {
                XmlInspectionRequest req = new XmlInspectionRequest();
                req.setXmlData(specification);
                req.setType(dsKind == DataShapeKinds.XML_SCHEMA ? io.atlasmap.xml.v2.InspectionType.SCHEMA : io.atlasmap.xml.v2.InspectionType.INSTANCE);
                return req;
            }
            case JSON_SCHEMA:
            case JSON_INSTANCE: {
                JsonInspectionRequest req = new JsonInspectionRequest();
                req.setJsonData(specification);
                req.setType(dsKind == DataShapeKinds.JSON_SCHEMA ? io.atlasmap.json.v2.InspectionType.SCHEMA : io.atlasmap.json.v2.InspectionType.INSTANCE);
                return req;
            }
            default: {
                fail("Bad datashape kind ({}) specified, only XML_SCHEMA, XML_INSTANCE, JSON_SCHEMA, JSON_INSTANCE are supported here", dsKind);
            }
        }
        return null;
    }


}
