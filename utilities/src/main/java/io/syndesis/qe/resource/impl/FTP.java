package io.syndesis.qe.resource.impl;

import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.LinkedList;
import java.util.List;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTP implements Resource {
    private static final String APP_NAME = "ftpd";
    private static final String LABEL_NAME = "app";

    @Override
    public void deploy() {
        if (!TestUtils.isDcDeployed(APP_NAME)) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName("ftp-cmd")
                .withContainerPort(2121)
                .withProtocol("TCP").build());

            for (int i = 0; i < 10; i++) {
                ContainerPort dataPort = new ContainerPortBuilder()
                    .withName("ftp-data-" + i)
                    .withContainerPort(2300 + i)
                    .withProtocol("TCP")
                    .build();
                ports.add(dataPort);
            }

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
                .addNewContainer().withName(APP_NAME).withImage("syndesisqe/ftpd:latest").addAllToPorts(ports)
                .endContainer()
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, APP_NAME);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName("ftp-cmd")
                .withPort(2121)
                .withTargetPort(new IntOrString(2121))
                .build());

            for (int i = 0; i < 10; i++) {
                serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                    .withName("ftp-data-" + i)
                    .withPort(2300 + i)
                    .withTargetPort(new IntOrString(2300 + i))
                    .build());
            }

            OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(APP_NAME)
                .addToLabels(LABEL_NAME, APP_NAME)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .done();
        }
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().deleteDeploymentConfig(OpenShiftUtils.getInstance().getDeploymentConfig(APP_NAME), true);
        OpenShiftUtils.getInstance().deleteService(OpenShiftUtils.getInstance().getService(APP_NAME));
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME));
    }
}
