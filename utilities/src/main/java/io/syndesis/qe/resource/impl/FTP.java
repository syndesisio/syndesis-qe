package io.syndesis.qe.resource.impl;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTP implements Resource {

    public static final int FTP_DATA_PORT = 2300;

    private final String labelName = "app";
    private String appName;
    public int ftpCommandPort; //on OCP

    public FTP() {
        this.initProperties();
    }

    @Override
    public void deploy() {
        if (!isDeployed()) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName("ftp-cmd")
                .withContainerPort(ftpCommandPort)
                .withProtocol("TCP").build());

            for (int i = 0; i < 10; i++) {
                ContainerPort dataPort = new ContainerPortBuilder()
                    .withName("ftp-data-" + i)
                    .withContainerPort(FTP_DATA_PORT + i)
                    .withProtocol("TCP")
                    .build();
                ports.add(dataPort);
            }

            OpenShiftUtils.getInstance().deploymentConfigs().createOrReplace(new DeploymentConfigBuilder()
                .editOrNewMetadata()
                .withName(appName)
                .addToLabels(labelName, appName)
                .endMetadata()

                .editOrNewSpec()
                .addToSelector(labelName, appName)
                .withReplicas(1)
                .editOrNewTemplate()
                .editOrNewMetadata()
                .addToLabels(labelName, appName)
                .endMetadata()
                .editOrNewSpec()
                .addNewContainer().withName(appName).withImage("quay.io/syndesis_qe/ftpd:latest").addAllToPorts(ports)
                .endContainer()
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .build());

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(labelName, appName);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName("ftp-cmd")
                .withPort(ftpCommandPort)
                .withTargetPort(new IntOrString(ftpCommandPort))
                .build());

            for (int i = 0; i < 10; i++) {
                serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                    .withName("ftp-data-" + i)
                    .withPort(FTP_DATA_PORT + i)
                    .withTargetPort(new IntOrString(FTP_DATA_PORT + i))
                    .build());
            }

            OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
                .editOrNewMetadata()
                .withName(appName)
                .addToLabels(labelName, appName)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .build());
        }
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().deleteDeploymentConfig(OpenShiftUtils.getInstance().getDeploymentConfig(appName), true);
        OpenShiftUtils.getInstance().deleteService(OpenShiftUtils.getInstance().getService(appName));
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(labelName, appName));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(appName);
    }

    private void initProperties() {
        Account account = AccountsDirectory.getInstance().get(Account.Name.FTP);
        Map<String, String> properties = new HashMap<>();
        account.getProperties().forEach((key, value) ->
            properties.put(key.toLowerCase(), value)
        );
        appName = properties.get("host");
        ftpCommandPort = Integer.parseInt(properties.get("port"));
    }
}
