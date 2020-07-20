package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SFTP implements Resource {

    public static final String TEST_DIRECTORY = "test";

    private final String labelName = "app";
    private final String serviceAccountName = "mysvcacct";
    private String appName;
    private int sftpPort;
    private String userAndPassword;

    public SFTP() {
        this.initProperties();
    }

    @Override
    public void deploy() {
        //        preparation for our specific SFTP image to fit Openshift requirements:
        OpenShiftUtils.getInstance().serviceAccounts()
            .createNew()
            .withNewMetadata()
            .withName(serviceAccountName)
            .endMetadata()
            .done();
        OpenShiftUtils.getInstance().securityContextConstraints().withName("anyuid").edit()
            .addNewUser("system:serviceaccount:" + TestConfiguration.openShiftNamespace() + ":" + serviceAccountName)
            .done();

        if (!isDeployed()) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName("sftp-cmd")
                .withContainerPort(sftpPort)
                .withProtocol("TCP").build());

            List<EnvVar> templateParams = new ArrayList<>();
            templateParams.add(new EnvVar("SFTP_USERS", userAndPassword, null));
            OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
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
                .addNewContainer().withName(appName).withImage("syndesisqe/sftpd-alp:latest")
                .addAllToPorts(ports)
                .addAllToEnv(templateParams)
                .endContainer()
                .withServiceAccount(serviceAccountName)
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(labelName, appName);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName("sftp-cmd")
                .withPort(sftpPort)
                .withTargetPort(new IntOrString(sftpPort))
                .build());

            OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(appName)
                .addToLabels(labelName, appName)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .done();
        }
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().deleteServiceAccount(OpenShiftUtils.getInstance().getServiceAccount(serviceAccountName));
        OpenShiftUtils.getInstance().securityContextConstraints().withName("anyuid").edit()
            .removeFromUsers("system:serviceaccount:" + TestConfiguration.openShiftNamespace() + ":" + serviceAccountName)
            .done();
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
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.SFTP);
        if (optional.isPresent()) {
            Map<String, String> properties = new HashMap<>();
            optional.get().getProperties().forEach((key, value) ->
                properties.put(key.toLowerCase(), value)
            );
            appName = properties.get("host");
            sftpPort = Integer.parseInt(properties.get("port"));
            userAndPassword = properties.get("username") + ":" + properties.get("password") + ":1500::" + TEST_DIRECTORY;
        } else {
            Assertions.fail("Credentials for " + Account.Name.SFTP + " were not found!");
        }
    }
}
