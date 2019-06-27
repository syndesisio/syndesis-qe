package io.syndesis.qe.templates;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KuduTemplate {
    private static final String APP_NAME = "syndesis-kudu";
    private static final String ROUTE_NAME = "kudu";
    private static final String LABEL_NAME = "syndesis.io/component";

    public static void deploy() {
        if (!TestUtils.isDcDeployed(APP_NAME)) {
            //OCP4HACK - openshift-client 4.3.0 isn't supported with OCP4 and can't create/delete templates, following line can be removed later
            OpenShiftUtils.binary().execute("create", "-f", Paths.get("../utilities/src/main/resources/templates/syndesis-kudu.yml").toAbsolutePath().toString());
//            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-kudu.yml")) {
//                OpenShiftUtils.getInstance().load(is).createOrReplace();
//            } catch (IOException e) {
//                fail("Template processing failed", e);
//            }

            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady(LABEL_NAME, APP_NAME), 15 * 60 * 1000L);
            } catch (InterruptedException | TimeoutException e) {
                fail("Wait for " + APP_NAME + " deployment failed!", e);
            }

            try {
                OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs(APP_NAME).contains("Flush successful"), 5 * 60 * 1000L);
            } catch (TimeoutException | InterruptedException e) {
                log.error(APP_NAME + " pod logs did not contain expected message. Pod logs:");
                log.error(OpenShiftUtils.getPodLogs(APP_NAME));
            }
        }
    }

    public static void cleanUp() {
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(APP_NAME)).findFirst()
                .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> APP_NAME.equals(service.getMetadata().getName())).findFirst()
                .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> ROUTE_NAME.equals(route.getMetadata().getName())).findFirst()
                .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
        OpenShiftUtils.getInstance().getPersistentVolumeClaims().stream()
                .filter(volume -> (volume.getMetadata().getName()).contains(APP_NAME))
                .forEach(volume -> OpenShiftUtils.getInstance().deletePersistentVolumeClaim(volume));
    }
}
