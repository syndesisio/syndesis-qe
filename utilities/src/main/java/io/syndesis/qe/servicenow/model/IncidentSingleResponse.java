package io.syndesis.qe.servicenow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Class representing response from api when there is just one incident object.
 */
@Data
public class IncidentSingleResponse {
    @JsonProperty("result")
    private Incident record;
}
