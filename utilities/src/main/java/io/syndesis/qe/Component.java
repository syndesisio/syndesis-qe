package io.syndesis.qe;

import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Syndesis;

import java.util.EnumSet;

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
    SERVER("syndesis-server"),
    UI ("syndesis-ui"),
    META("syndesis-meta"),
    DV("syndesis-dv"),
    JAEGER("syndesis-jaeger");

    private final String name;

    Component(String name) {
        this.name = name;
    }

    /**
     * Gets all the components that are currently enabled.
     *
     * @return enumset of all currently used components
     */
    public static EnumSet<Component> getAllComponents() {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        EnumSet<Component> ret = EnumSet.of(OAUTH, PROMETHEUS, SERVER, UI, META);

        if (syndesis.isAddonEnabled(Addon.DV)) {
            ret.add(DV);
        }

        if (syndesis.isAddonEnabled(Addon.JAEGER)) {
            ret.add(JAEGER);
        }

        if (!syndesis.isAddonEnabled(Addon.EXTERNAL_DB)) {
            ret.add(DB);
        }
        return ret;
    }
}
