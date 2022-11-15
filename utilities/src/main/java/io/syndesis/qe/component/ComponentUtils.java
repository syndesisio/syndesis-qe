package io.syndesis.qe.component;

import static io.syndesis.qe.component.Component.DB;
import static io.syndesis.qe.component.Component.FUSE_ONLINE_OPERATOR;
import static io.syndesis.qe.component.Component.JAEGER;
import static io.syndesis.qe.component.Component.META;
import static io.syndesis.qe.component.Component.OAUTH;
import static io.syndesis.qe.component.Component.OPERATOR;
import static io.syndesis.qe.component.Component.PROMETHEUS;
import static io.syndesis.qe.component.Component.SERVER;
import static io.syndesis.qe.component.Component.TODO;
import static io.syndesis.qe.component.Component.UI;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.addon.Addon;
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
public class ComponentUtils {
    /**
     * Gets all the components that are currently enabled.
     *
     * @return enumset of all currently used components
     */
    public static EnumSet<Component> getAllComponents() {
        Syndesis syndesis = ResourceFactory.get(Syndesis.class);
        EnumSet<Component> components = EnumSet.of(OAUTH, PROMETHEUS, SERVER, UI, META, OPERATOR);

        // For disconnected cluster testing, this addon needs to be enabled for demo data, but won't actually run (cannot get to source code on github)
        if (syndesis.isAddonEnabled(Addon.TODO) && !TestConfiguration.isDisconnecterEnvironment()) {
            components.add(TODO);
        }

        // don't count jaeger addon when operatorOnly is set to true (Jaeger cr has to be created manually)
        if (syndesis.isAddonEnabled(Addon.JAEGER) && !syndesis.containsAddonPropertyAndBooleanValue(Addon.JAEGER, "operatorOnly", true)) {
            components.add(JAEGER);
        }

        if (!syndesis.isAddonEnabled(Addon.EXTERNAL_DB)) {
            components.add(DB);
        }

        if (TestConfiguration.isOperatorHubInstall()) {
            components.remove(OPERATOR);
            components.add(FUSE_ONLINE_OPERATOR);
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
