package io.syndesis.qe.model;

import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.integration.IntegrationDeploymentState;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
    private IntegrationBulletinBoard board;
}
