package io.syndesis.qe;

import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Syndesis;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.Getter;

/**
 * Idea of this enum is to cover useful values wrt to Syndesis infra components
 * E.g. ports, service names, etc.
 */
@Getter
public enum Component {
    DB("syndesis-db"),
    OAUTH("syndesis-oauthproxy"),
    PROMETHEUS("syndesis-prometheus"),
    SERVER("syndesis-server"),
    UI("syndesis-ui"),
    META("syndesis-meta"),
    OPERATOR("syndesis-operator"),
    DV("syndesis-dv"),
    TODO("todo"),
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
        EnumSet<Component> components = EnumSet.of(OAUTH, PROMETHEUS, SERVER, UI, META, OPERATOR);

        if (syndesis.isAddonEnabled(Addon.TODO)) {
            components.add(TODO);
        }

        if (syndesis.isAddonEnabled(Addon.DV)) {
            components.add(DV);
        }

        if (syndesis.isAddonEnabled(Addon.JAEGER)) {
            components.add(JAEGER);
        }

        if (!syndesis.isAddonEnabled(Addon.EXTERNAL_DB)) {
            components.add(DB);
        }
        return components;
    }

    /**
     * Gets the pods of all syndesis infrastructure components, excluding integration pods.
     *
     * @return list of pods of syndesis components
     */
    public static List<Pod> getComponentPods() {
        return OpenShiftUtils.getInstance().pods().withLabel("syndesis.io/component").list().getItems().stream()
            .filter(p -> !StringUtils.endsWithAny(p.getMetadata().getName(), new String[] {"build", "deploy"}))
            .filter(p -> !"integration".equals(p.getMetadata().getLabels().get("syndesis.io/component")))
            .collect(Collectors.toList());
    }
}
