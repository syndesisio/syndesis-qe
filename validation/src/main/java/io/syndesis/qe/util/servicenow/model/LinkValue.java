package io.syndesis.qe.util.servicenow.model;

import lombok.Data;

/**
 * Represents link/value json structure.
 */
@Data
public class LinkValue {
    private String link;
    private String value;

    public LinkValue() {
    }

    public LinkValue(String value) {
        this.value = value;
    }
}
