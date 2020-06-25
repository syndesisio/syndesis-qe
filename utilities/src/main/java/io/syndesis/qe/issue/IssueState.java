package io.syndesis.qe.issue;

import lombok.Getter;
import lombok.Setter;

public enum IssueState {
    OPEN,
    CLOSED,
    DONE;

    public enum DoneJira {
        DONE("Done"),
        RESOLVED("Resolved"),
        PRODUCTIZATION_BACKLOG("Productization Backlog"),
        IN_PRODUCTIZATION("In Productization"),
        VALIDATION_BACKLOG("Validation Backlog"),
        IN_VALIDATION("In Validation");

        @Setter
        @Getter
        private String id;

        DoneJira(String id) {
            this.id = id;
        }
    }
}
