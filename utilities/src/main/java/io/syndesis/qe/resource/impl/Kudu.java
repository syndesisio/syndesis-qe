package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kudu implements Resource {
    private static final String APP_NAME = "syndesis-kudu";
    private static final String API_APP_NAME = "kudu-rest-api";
    private static final String ROUTE_NAME = "kudu";
    private static final String LABEL_NAME = "syndesis.io/component";

    @Override
    public void deploy() {
        if (!TestUtils.isDcDeployed(APP_NAME)) {
            //OCP4HACK - openshift-client 4.3.0 isn't supported with OCP4 and can't create/delete templates, following line can be removed later
            OpenShiftUtils.binary()
                .execute("create", "-f", Paths.get("../utilities/src/main/resources/templates/syndesis-kudu.yml").toAbsolutePath().toString());
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

            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName("kudu-rest-api")
                .withContainerPort(8080)
                .build());

            OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(API_APP_NAME)
                .addToLabels(LABEL_NAME, API_APP_NAME)
                .endMetadata()

                .editOrNewSpec()
                .addToSelector(LABEL_NAME, API_APP_NAME)
                .withReplicas(1)
                .editOrNewTemplate()
                .editOrNewMetadata()
                .addToLabels(LABEL_NAME, API_APP_NAME)
                .endMetadata()
                .editOrNewSpec()
                .addNewContainer().withName(API_APP_NAME).withImage("mcada/syndesis-kudu-rest-api:latest").addAllToPorts(ports)

                .endContainer()
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, API_APP_NAME);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName("kudu-rest-api-service")
                .withPort(8080)
                .withTargetPort(new IntOrString(8080))
                .build());

            OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(API_APP_NAME)
                .addToLabels(LABEL_NAME, API_APP_NAME)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .done();

            final Route route = new RouteBuilder()
                .withNewMetadata()
                .withName(API_APP_NAME)
                .endMetadata()
                .withNewSpec()
                .withPath("/")
                .withWildcardPolicy("None")
                .withNewTls()
                .withTermination("edge")
                .withInsecureEdgeTerminationPolicy("Allow")
                .endTls()
                .withNewTo()
                .withKind("Service").withName(API_APP_NAME)
                .endTo()
                .endSpec()
                .build();

            log.info("Creating route {} with path {}", API_APP_NAME, "/");
            OpenShiftUtils.getInstance().routes().createOrReplace(route);

            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsReady(LABEL_NAME, API_APP_NAME, 1), 15 * 60 * 1000L);
                log.info("Checking pod logs if the app is ready");
                OpenShiftWaitUtils.waitFor(() -> !OpenShiftUtils.arePodLogsEmpty(API_APP_NAME), 5 * 60 * 1000L);
                OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs(API_APP_NAME).contains("Started App in"), 5 * 60 * 1000L);
            } catch (InterruptedException | TimeoutException e) {
                fail("Wait for " + API_APP_NAME + " deployment failed ", e);
            }
        }
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(APP_NAME)).findFirst()
            .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> APP_NAME.equals(service.getMetadata().getName())).findFirst()
            .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> ROUTE_NAME.equals(route.getMetadata().getName())).findFirst()
            .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
        OpenShiftUtils.getInstance().getPersistentVolumeClaims().stream()
            .filter(volume -> (volume.getMetadata().getName()).contains(APP_NAME))
            .forEach(volume -> OpenShiftUtils.getInstance().deletePersistentVolumeClaim(volume));

        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(API_APP_NAME)).findFirst()
            .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> API_APP_NAME.equals(service.getMetadata().getName())).findFirst()
            .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> API_APP_NAME.equals(route.getMetadata().getName())).findFirst()
            .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
    }
}
