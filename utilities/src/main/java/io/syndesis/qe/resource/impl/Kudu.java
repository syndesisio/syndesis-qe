package io.syndesis.qe.resource.impl;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kudu implements Resource {
    private static final String APP_NAME = "syndesis-kudu";
    private static final String LABEL_NAME = "app";
    private static final String ROUTE_NAME = "kudu";
    private static final String API_APP_NAME = "kudu-rest-api";
    public static final int KUDU_PORT = 7051;
    public static final int REST_PORT = 8080;
    public static final String MOUNT_NAME_MASTER = "syndesis-kudu-master";
    public static final String MOUNT_NAME_TSERVER = "syndesis-kudu-tserver";
    public static final String MOUNT_PATH_MASTER = "/var/lib/kudu/master";
    public static final String MOUNT_PATH_TSERVER = "/var/lib/kudu/tserver";

    @Override
    public void deploy() {
        if (!OpenShiftUtils.isDcDeployed(APP_NAME)) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName(APP_NAME)
                .withContainerPort(KUDU_PORT)
                .withProtocol("TCP").build());

            List<EnvVar> templateParams = new ArrayList<>();
            OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(APP_NAME)
                .addToLabels(LABEL_NAME, APP_NAME)
                .endMetadata()
                .editOrNewSpec()
                .addToSelector(LABEL_NAME, APP_NAME)
                .withReplicas(1)
                .editOrNewTemplate()
                .editOrNewMetadata()
                .addToLabels(LABEL_NAME, APP_NAME)
                .endMetadata()
                .editOrNewSpec()
                .addNewContainer().withName(APP_NAME).withImage("quay.io/syndesis_qe/kudu-2in1:latest")
                .addAllToPorts(ports)
                .addAllToEnv(templateParams)
                .addNewVolumeMount().withName(MOUNT_NAME_MASTER)
                .withMountPath(MOUNT_PATH_MASTER).withReadOnly(false)
                .endVolumeMount()
                .addNewVolumeMount().withName(MOUNT_NAME_TSERVER)
                .withMountPath(MOUNT_PATH_TSERVER).withReadOnly(false)
                .endVolumeMount()
                .endContainer()
                .addNewVolume()
                .withName(MOUNT_NAME_MASTER)
                .withNewEmptyDir().endEmptyDir()
                .endVolume()
                .addNewVolume()
                .withName(MOUNT_NAME_TSERVER)
                .withNewEmptyDir().endEmptyDir()
                .endVolume()
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, APP_NAME);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName(APP_NAME)
                .withPort(KUDU_PORT)
                .withTargetPort(new IntOrString(KUDU_PORT))
                .build());

            OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(APP_NAME)
                .addToLabels(LABEL_NAME, APP_NAME)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .done();
        }

        if (!OpenShiftUtils.isDcDeployed(API_APP_NAME)) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName(API_APP_NAME)
                .withContainerPort(REST_PORT)
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
                .addNewContainer().withName(API_APP_NAME).withImage("quay.io/syndesis_qe/kudu-rest-api:latest").addAllToPorts(ports)
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
                .withPort(REST_PORT)
                .withTargetPort(new IntOrString(REST_PORT))
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
        }

        createAccount();
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

        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(API_APP_NAME))
            .findFirst()
            .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> API_APP_NAME.equals(service.getMetadata().getName()))
            .findFirst()
            .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> API_APP_NAME.equals(route.getMetadata().getName())).findFirst()
            .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME))
            && OpenShiftUtils.getPodLogs(APP_NAME).contains("Flush successful")
            && OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, API_APP_NAME))
            && OpenShiftUtils.getPodLogs(API_APP_NAME).contains("Started App in");
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(APP_NAME);
    }

    public void createAccount() {
        Account kudu = new Account();
        kudu.setService("Apache Kudu");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("host", "syndesis-kudu");
        kudu.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount("kudu", kudu);
    }
}
