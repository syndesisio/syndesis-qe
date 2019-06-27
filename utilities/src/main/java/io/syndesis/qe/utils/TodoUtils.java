package io.syndesis.qe.utils;

import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TodoUtils {
    private TodoUtils() {
    }

    public static void createDefaultRouteForTodo(String name, String path) {
        final Route route = new RouteBuilder()
                .withNewMetadata()
                .withName(name)
                .endMetadata()
                .withNewSpec()
                .withPath(path)
                .withWildcardPolicy("None")
                .withNewTls()
                .withTermination("edge")
                .withInsecureEdgeTerminationPolicy("Allow")
                .endTls()
                .withNewTo()
                .withKind("Service").withName("todo")
                .endTo()
                .endSpec()
                .build();
        log.info("Creating route {} with path {}", name, path);
        OpenShiftUtils.getInstance().routes().createOrReplace(route);
    }
}
