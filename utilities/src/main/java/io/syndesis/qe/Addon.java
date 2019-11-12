package io.syndesis.qe;

import lombok.Getter;

/**
 * Enum of all addons that can be configured in Syndesis CR.
 */
public enum Addon {
    CAMELK("camelk"),
    DV("dv"),
    JAEGER("jaeger"),
    OPS("ops"),
    TODO("todo"),
    // Technically not an addon
    EXTERNAL_DB("db");

    @Getter
    private String value;

    Addon(String value) {
        this.value = value;
    }
}
