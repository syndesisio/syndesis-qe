package io.syndesis.qe.resource.impl;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.junit.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FHIR implements Resource {
    private final String labelName = "app";
    public int fhirPort;
    private String appName;

    @Override
    public void deploy() {
        addAccount();
        initProperties();
        if (!OpenShiftUtils.isDcDeployed(appName)) {

            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                .withName(appName)
                .withContainerPort(fhirPort)
                .withProtocol("TCP").build());

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
                .addNewContainer().withName(appName).withImage("syndesisqe/hapi-fhir-fab:latest").addAllToPorts(ports)
                .endContainer()
                .endSpec()
                .endTemplate()
                .addNewTrigger()
                .withType("ConfigChange")
                .endTrigger()
                .endSpec()
                .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(labelName, appName);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                .withName(appName)
                .withPort(fhirPort)
                .withTargetPort(new IntOrString(fhirPort))
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
        if (isDeployed()) {
            OpenShiftUtils.getInstance().deleteDeploymentConfig(OpenShiftUtils.getInstance().getDeploymentConfig(appName), true);
            OpenShiftUtils.getInstance().deleteService(OpenShiftUtils.getInstance().getService(appName));
            OpenShiftWaitUtils.waitUntilPodIsDeleted(appName);
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(labelName, appName))
            && OpenShiftUtils.getPodLogs(appName).contains("Server:main: Started");
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(appName);
    }

    private void initProperties() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.FHIR);
        if (optional.isPresent()) {
            Map<String, String> properties = new HashMap<>();
            optional.get().getProperties().forEach((key, value) ->
                properties.put(key.toLowerCase(), value)
            );
            appName = properties.get("host");
            fhirPort = Integer.parseInt(properties.get("port"));
        } else {
            Assert.fail("Credentials for " + Account.Name.FHIR + " were not found!");
        }
    }

    public void addAccount() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.FHIR);
        if (!optional.isPresent()) {
            Account fhir = new Account();
            Map<String, String> fhirParameters = new HashMap<>();
            fhirParameters.put("port", "8080");
            fhirParameters.put("host", "fhir-app");
            fhirParameters.put("serverurl", "http://fhir-app:8080/baseDstu3");

            fhir.setService("fhir-app");
            fhir.setProperties(fhirParameters);
            AccountsDirectory.getInstance().getAccounts().put("fhir", fhir);
        }
    }
}
