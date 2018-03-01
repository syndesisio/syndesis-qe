package io.syndesis.qe;

import lombok.Getter;

/**
 * Idea of this enum is to cover useful values wrt to Syndesis infra components
 * E.g. ports, service names, etc.
 */
@Getter
public enum Component {

    DB ("syndesis-db"),
    OAUTH ("syndesis-oauthproxy"),
    PROMETHEUS ("syndesis-prometheus"),
    REST ("syndesis-rest"),
    UI ("syndesis-ui"),
    VERIFIER ("syndesis-verifier");

    private final String name;

    Component(String name) {
        this.name = name;
    }

}
