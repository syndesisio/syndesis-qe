package io.syndesis.qe.resource.impl;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.endpoint.ConnectionsEndpoint;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * This resource substitute syndesis-db when the external db is used
 */
@Slf4j
public class SyndesisDB implements Resource {

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    public static final String DEFAULT_PSQL_CONNECTION_BACKUP = "PostgresDB_ORIGINAL_BACKUP";
    public static final String DEFAULT_PSQL_CONNECTION_ORIGINAL = "PostgresDB";

    private static final String APP_NAME = "syndesis-db";
    private static final String LABEL_NAME = "app";
    private static final String DB_ROOT_USER = "developer";
    private static final String DB_SAMPLE_DB_USER = "sampledb";
    private static final String DB_PASSWORD = "developer";
    private static final String DB_SCHEMA = "sampledb";
    private static final String DB_URL = "jdbc:postgresql://syndesis-db:5432/sampledb";

    @Override
    public void deploy() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName("psql-cmd")
            .withContainerPort(5432)
            .withProtocol("TCP").build());

        List<EnvVar> templateParams = new ArrayList<>();
        templateParams.add(new EnvVar("POSTGRESQL_USER", DB_ROOT_USER, null));
        templateParams.add(new EnvVar("POSTGRESQL_PASSWORD", DB_PASSWORD, null));
        //sample DB schema is created by post start script, this DB is not important
        templateParams.add(new EnvVar("POSTGRESQL_DATABASE", "whatever", null));
        templateParams.add(new EnvVar("POSTGRESQL_SAMPLEDB_PASSWORD", DB_PASSWORD, null));

        //config map with the post start script
        if (OpenShiftUtils.getInstance().getConfigMap("syndesis-sampledb-config") == null) {
            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-sampledb-config.yml")) {
                ConfigMap cm = OpenShiftUtils.getInstance().configMaps().load(is).get();
                OpenShiftUtils.getInstance().createConfigMap(cm);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read config map ", ex);
            }
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
            .addNewContainer().withName(APP_NAME).withImage("quay.io/syndesis_qe/postgresql-10-centos7:latest").addAllToPorts(ports).addAllToEnv(templateParams)

            // Post start script for creating all databases and procedures in syndesisDB
            .editOrNewLifecycle()
            .editOrNewPostStart()
            .editOrNewExec()
            .addNewCommand("/bin/sh")
            .addNewCommand("-c")
            .addNewCommand("/var/lib/pgsql/sampledb/postStart.sh")
            .endExec()
            .endPostStart()
            .endLifecycle()
            .addNewVolumeMount()
            .withNewMountPath("/var/lib/pgsql/sampledb")
            .withName("syndesis-sampledb-config")
            .endVolumeMount()
            .endContainer()
            .addNewVolume()
            .editOrNewConfigMap()
            .withName("syndesis-sampledb-config")
            .withDefaultMode(511)
            .endConfigMap()
            .withName("syndesis-sampledb-config")
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
            .withName("psql-cmd")
            .withPort(5432)
            .withTargetPort(new IntOrString(5432))
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

        Account syndesisDbAccount = new Account();
        syndesisDbAccount.setService("SyndesisDB");
        Map<String, String> accountParameters = new HashMap<>();
        accountParameters.put("url", DB_URL);
        accountParameters.put("user", DB_SAMPLE_DB_USER);
        accountParameters.put("password", DB_PASSWORD);
        accountParameters.put("schema", DB_SCHEMA);
        syndesisDbAccount.setProperties(accountParameters);
        AccountsDirectory.getInstance().addAccount(Account.Name.SYNDESIS_DB.getId(), syndesisDbAccount);

        Connection defaultPostgresDBConnection = connectionsEndpoint.getConnectionByName(DEFAULT_PSQL_CONNECTION_ORIGINAL);

        // backup default connection
        // (When the test suite was killed before, the backup can exist and original connection was already changed, ignore in that case
        if (connectionsEndpoint.getConnectionByName(DEFAULT_PSQL_CONNECTION_BACKUP) == null) {
            connectionsEndpoint.create(defaultPostgresDBConnection.builder().name(DEFAULT_PSQL_CONNECTION_BACKUP).id("1000").build());
        }

        //config prop for new instance
        Map<String, String> configuredProperties = new HashMap<>(defaultPostgresDBConnection.getConfiguredProperties());
        configuredProperties.put("schema", DB_SCHEMA);
        configuredProperties.put("password", DB_PASSWORD);
        configuredProperties.put("user", DB_SAMPLE_DB_USER);
        configuredProperties.put("url", DB_URL);

        Connection build = defaultPostgresDBConnection.builder().configuredProperties(configuredProperties).build();
        connectionsEndpoint.update(defaultPostgresDBConnection.getId().get(), build);
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
            log.error(
                "Error thrown while trying to delete Syndesis DB PostgreSQL database. It is just deletion, it should not affect following tests.", e);
        }
        // revert changes if the default connection was changes
        if (connectionsEndpoint.getConnectionByName(DEFAULT_PSQL_CONNECTION_BACKUP) != null) {
            Connection backupConnection = connectionsEndpoint.getConnectionByName(DEFAULT_PSQL_CONNECTION_BACKUP);
            Connection defaultConnection = connectionsEndpoint.getConnectionByName(DEFAULT_PSQL_CONNECTION_ORIGINAL);

            Map<String, String> originalConfiguredProperties = new HashMap<>(backupConnection.getConfiguredProperties());
            Connection build = defaultConnection.builder().configuredProperties(originalConfiguredProperties).build();
            connectionsEndpoint.update(defaultConnection.getId().get(), build);
            connectionsEndpoint.delete(backupConnection.getId().get());
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME))
            && OpenShiftUtils.getPodLogs(APP_NAME).contains("***** sampledb created"); //syndesisDB post script from configmap was successfully loaded
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME).isPresent();
    }
}
