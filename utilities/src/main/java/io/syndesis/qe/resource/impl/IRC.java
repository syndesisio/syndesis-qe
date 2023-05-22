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
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IRC implements Resource {
    private static final String LABEL_NAME = "app";
    private static final String SERVER_APP_NAME = "irc-server";
    public static final String CONTROLLER_APP_NAME = "irc-controller";

    private static final int IRC_PORT = 6667;
    private static final int CONTROLLER_PORT = 8080;

    @Override
    public void deploy() {
        if (!OpenShiftUtils.isDcDeployed(SERVER_APP_NAME)) {
            deployIrcServer();
        }

        if (!OpenShiftUtils.isDcDeployed(CONTROLLER_APP_NAME)) {
            deployIrcController();
        }

        addAccount();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().deploymentConfigs().list().getItems().stream().filter(
            dc -> dc.getMetadata().getName().startsWith("irc-")
        ).forEach(
            dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true)
        );

        OpenShiftUtils.getInstance().services().delete(
            OpenShiftUtils.getInstance().services().list().getItems().stream().filter(
                s -> s.getMetadata().getName().startsWith("irc-")).collect(Collectors.toList())
        );

        OpenShiftUtils.getInstance().routes().withName(CONTROLLER_APP_NAME).delete();
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, SERVER_APP_NAME))
            && OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, CONTROLLER_APP_NAME));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(SERVER_APP_NAME);
    }

    private static void deployIrcServer() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName(SERVER_APP_NAME)
            .withContainerPort(IRC_PORT)
            .withProtocol("TCP").build());

        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplace(new DeploymentConfigBuilder()
            .editOrNewMetadata()
            .withName(SERVER_APP_NAME)
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()

            .editOrNewSpec()
            .addToSelector(LABEL_NAME, SERVER_APP_NAME)
            .withReplicas(1)
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()
            .editOrNewSpec()
            .addNewContainer().withName(SERVER_APP_NAME).withImage("quay.io/syndesis_qe/irc:latest").addAllToPorts(ports)

            .endContainer()
            .endSpec()
            .endTemplate()
            .addNewTrigger()
            .withType("ConfigChange")
            .endTrigger()
            .endSpec()
            .build());

        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, SERVER_APP_NAME);

        serviceSpecBuilder.addToPorts(new ServicePortBuilder()
            .withName(SERVER_APP_NAME)
            .withPort(IRC_PORT)
            .withTargetPort(new IntOrString(IRC_PORT))
            .build());

        OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
            .withNewMetadata()
            .withName(SERVER_APP_NAME)
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()
            .withNewSpecLike(serviceSpecBuilder.build())
            .endSpec()
            .build());
    }

    private static void deployIrcController() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName(CONTROLLER_APP_NAME)
            .withContainerPort(CONTROLLER_PORT)
            .withProtocol("TCP").build());

        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(new EnvVar("HOST", SERVER_APP_NAME, null));

        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplace(new DeploymentConfigBuilder()
            .editOrNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()

            .editOrNewSpec()
            .addToSelector(LABEL_NAME, CONTROLLER_APP_NAME)
            .withReplicas(1)
            .editOrNewTemplate()
            .editOrNewMetadata()
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()
            .editOrNewSpec()
            .addNewContainer().withName(CONTROLLER_APP_NAME).withImage("quay.io/syndesis_qe/irc-controller:latest").addAllToPorts(ports).addAllToEnv(envVars)

            .endContainer()
            .endSpec()
            .endTemplate()
            .addNewTrigger()
            .withType("ConfigChange")
            .endTrigger()
            .endSpec()
            .build());

        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, CONTROLLER_APP_NAME);

        serviceSpecBuilder.addToPorts(new ServicePortBuilder()
            .withName(CONTROLLER_APP_NAME)
            .withPort(CONTROLLER_PORT)
            .withTargetPort(new IntOrString(CONTROLLER_PORT))
            .build());

        OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
            .editOrNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()
            .editOrNewSpecLike(serviceSpecBuilder.build())
            .endSpec()
            .build());

        OpenShiftUtils.getInstance().routes().createOrReplace(new RouteBuilder()
            .withNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .endMetadata()
            .withNewSpec()
            .withPath("/")
            .withWildcardPolicy("None")
            .withNewTls()
            .withTermination("edge")
            .withInsecureEdgeTerminationPolicy("Allow")
            .endTls()
            .withNewTo()
            .withKind("Service").withName(CONTROLLER_APP_NAME)
            .endTo()
            .endSpec()
            .build());
    }

    public void addAccount() {
        Account irc = new Account();
        Map<String, String> params = new HashMap<>();
        params.put("hostname", "irc-server");
        params.put("port", "6667");
        irc.setService("irc");
        irc.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("irc", irc);
    }
}
