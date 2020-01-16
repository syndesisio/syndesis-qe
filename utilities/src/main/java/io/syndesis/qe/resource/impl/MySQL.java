package io.syndesis.qe.resource.impl;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySQL implements Resource {
    private static final String APP_NAME = "mysql";
    private static final String LABEL_NAME = "app";
    private static final String DB_USER = "developer";
    private static final String DB_PASSWORD = "developer";
    private static final String DB_SCHEMA = "sampledb";
    private static final String DB_URL = "jdbc:mysql://mysql:3306/sampledb";

    @Override
    public void deploy() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
                .withName("mysql-cmd")
                .withContainerPort(3306)
                .withProtocol("TCP").build());

        List<EnvVar> templateParams = new ArrayList<>();
        templateParams.add(new EnvVar("MYSQL_USER", DB_USER, null));
        templateParams.add(new EnvVar("MYSQL_PASSWORD", DB_PASSWORD, null));
        templateParams.add(new EnvVar("MYSQL_DATABASE", DB_SCHEMA, null));

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
                .addNewContainer().withName(APP_NAME).withImage("centos/mysql-57-centos7").addAllToPorts(ports).addAllToEnv(templateParams)

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
                .withName("mysql-cmd")
                .withPort(3306)
                .withTargetPort(new IntOrString(3306))
                .build());

        OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
                .editOrNewMetadata()
                .withName(APP_NAME)
                .addToLabels(LABEL_NAME, APP_NAME)
                .endMetadata()
                .editOrNewSpecLike(serviceSpecBuilder.build())
                .endSpec()
                .done();

        try {
            OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsReady(LABEL_NAME, APP_NAME, 1));
            Thread.sleep(20 * 1000);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Wait for {} deployment failed ", APP_NAME, e);
        }

        Account mysqlAccount = new Account();
        mysqlAccount.setService("mysql");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("url", DB_URL);
        accountParameters.put("user", DB_USER);
        accountParameters.put("password", DB_PASSWORD);
        accountParameters.put("schema", DB_SCHEMA);
        mysqlAccount.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount("mysql", mysqlAccount);
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
            log.error("Error thrown while trying to delete mysql database. It is just deletion, it should not affect following tests.", e);
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME))
            && OpenShiftUtils.getPodLogs("mysql").contains("MySQL started successfully");
    }
}
