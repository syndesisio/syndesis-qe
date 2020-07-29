package io.syndesis.qe.endpoint.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.json.v2.InspectionType;

/**
 * Feb 28, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonInspectionRequest {

    protected String jsonData;
    protected String uri;
    protected InspectionType type;
}
