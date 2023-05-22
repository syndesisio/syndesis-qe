package io.syndesis.qe.resource.impl;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
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
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDb36 implements Resource {
    public static final int MONGODB_PORT = 27017;
    public static final String APP_NAME = "mongodb36";
    private static final String LABEL_NAME = "app";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String MONGODB_USER = "user";
    private static final String MONGODB_PASSWORD = "user";
    private static final String MONGODB_DATABASE = "sampledb";
    private static final String MONGODB_IMAGE_REPOSITORY = System.getProperty("mongo.docker.registry", "docker.io");
    private static final String MONGODB_IMAGE = MONGODB_IMAGE_REPOSITORY + (MONGODB_IMAGE_REPOSITORY.contains("@") ? "" : "/bitnami/mongodb:3.6.16");
    private static final String MONGDB_URL = "mongodb://user:user@mongodb/sampledb";
    private static final String MONGODB_REPLICA_SET_NAME = "rs0";
    private static final String MONGODB_REPLICA_SET_MODE = "primary";
    private static final String MONGODB_REPLICA_SET_KEY = "replica";

    @Override
    public void deploy() {
        addAccount();

        if (isDeployed()) {
            return;
        }

        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName("mongodb")
            .withContainerPort(MONGODB_PORT)
            .withProtocol("TCP").build());

        List<EnvVar> templateParams = new ArrayList<>();
        templateParams.add(new EnvVar("MONGODB_ADMIN_PASSWORD", ADMIN_PASSWORD, null));
        templateParams.add(new EnvVar("MONGODB_USERNAME", MONGODB_USER, null));
        templateParams.add(new EnvVar("MONGODB_PASSWORD", MONGODB_PASSWORD, null));
        templateParams.add(new EnvVar("MONGODB_DATABASE", MONGODB_DATABASE, null));
        templateParams.add(new EnvVar("MONGODB_REPLICA_SET_NAME", MONGODB_REPLICA_SET_NAME, null));
        templateParams.add(new EnvVar("MONGODB_REPLICA_SET_MODE", MONGODB_REPLICA_SET_MODE, null));
        templateParams.add(new EnvVar("MONGODB_REPLICA_SET_KEY", MONGODB_REPLICA_SET_KEY, null));
        templateParams.add(new EnvVar("MONGODB_ROOT_PASSWORD", MONGODB_REPLICA_SET_KEY, null));

        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplace(new DeploymentConfigBuilder()
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
            .addNewContainer().withName(APP_NAME).withImage(MONGODB_IMAGE)
            .addAllToPorts(ports)
            .addAllToEnv(templateParams)

            .endContainer()
            .endSpec()

            .endTemplate()
            .addNewTrigger()
            .withType("ConfigChange")
            .endTrigger()
            .endSpec()
            .build());

        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, APP_NAME);

        serviceSpecBuilder.addToPorts(new ServicePortBuilder()
            .withName("mongodb")
            .withPort(MONGODB_PORT)
            .withTargetPort(new IntOrString(MONGODB_PORT))
            .build());

        OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
            .editOrNewMetadata()
            .withName(APP_NAME)
            .addToLabels(LABEL_NAME, APP_NAME)
            .endMetadata()
            .editOrNewSpecLike(serviceSpecBuilder.build())
            .endSpec()
            .build());
    }

    @Override
    public void undeploy() {
        try {
            OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(APP_NAME)).findFirst()
                .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
            OpenShiftUtils.getInstance().getServices().stream().filter(service -> APP_NAME.equals(service.getMetadata().getName())).findFirst()
                .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
            TestUtils.sleepIgnoreInterrupt(5000);
        } catch (Exception e) {
            log.error("Error thrown while trying to delete mongodb database. It is just deletion, it should not affect following tests.", e);
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME)) && OpenShiftUtils.getPodLogs(APP_NAME).contains("transition to primary complete; database writes are now permitted");
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(APP_NAME);
    }

    public void addAccount() {
        Account mongodbAccount = new Account();
        mongodbAccount.setService("mongodb36");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("host", APP_NAME);
        accountParameters.put("user", MONGODB_USER);
        accountParameters.put("password", MONGODB_PASSWORD);
        accountParameters.put("database", MONGODB_DATABASE);
        // this does not work for now
        //        accountParameters.put("admindb", MONGODB_DATABASE);
        accountParameters.put("url", MONGDB_URL);
        mongodbAccount.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount(Account.Name.MONGODB36.getId(), mongodbAccount);
    }
}
