package io.syndesis.qe.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.integration.IntegrationDeploymentState;
import lombok.Data;

/**
 * Feb 16, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@JsonDeserialize
@Data
public class IntegrationOverview {

    public IntegrationOverview() {
    }

    private int version;
    private String id;
    private String name;
    private boolean draft;
    private IntegrationDeploymentState currentState;
    private IntegrationDeploymentState targetState;
}
