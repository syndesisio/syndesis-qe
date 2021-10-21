package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kafka implements Resource {

    private static String RESOURCES_FOLDER = "../utilities/src/main/resources/kafka/amq-streams-%s-cluster-operator";
    private static String KAFKA_CR = "../utilities/src/main/resources/kafka/kafka-ephemeral-%s.yaml";

    public Kafka() {
        if (OpenShiftUtils.isOpenshift3()) {
            RESOURCES_FOLDER = Paths.get(String.format(RESOURCES_FOLDER, "1.6")).toAbsolutePath().toString();
            KAFKA_CR = Paths.get(String.format(KAFKA_CR, "1.6")).toAbsolutePath().toString();
        } else {
            RESOURCES_FOLDER = Paths.get(String.format(RESOURCES_FOLDER, "1.8")).toAbsolutePath().toString();
            KAFKA_CR = Paths.get(String.format(KAFKA_CR, "1.8")).toAbsolutePath().toString();
        }
    }

    @Override
    public void deploy() {
        // Replace namespace in the resources
        File folder = new File(RESOURCES_FOLDER);
        for (String resourceName : folder.list()) {
            File resource = new File(RESOURCES_FOLDER, resourceName);
            if (resourceName.contains("RoleBinding")) {
                TestUtils.replaceInFile(resource, "namespace: .*\n", String.format("namespace: %s\n", TestConfiguration.openShiftNamespace()));
            }
            try (FileInputStream resourceIS = new FileInputStream(resource)) {
                OpenShiftUtils.getInstance().load(resourceIS).get().forEach(res -> {
                    OpenShiftUtils.getInstance().resource(res).createOrReplace();
                });
            } catch (IOException e) {
                InfraFail.fail("IO exception during creating Kafka resource", e);
            }
        }
        OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("strimzi.io/kind", "cluster-operator"));
        OpenShiftUtils.create(KAFKA_CR);
        addAccounts();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.delete(KAFKA_CR);
        File folder = new File(RESOURCES_FOLDER);
        for (String resourceName : folder.list()) {
            try (FileInputStream resourceIS = new FileInputStream(new File(RESOURCES_FOLDER, resourceName))) {
                OpenShiftUtils.getInstance().load(resourceIS).get().forEach(res -> {
                    if (!(res instanceof CustomResourceDefinition || res instanceof ClusterRole)) {
                        OpenShiftUtils.getInstance().resource(res).cascading(true).delete();
                    }
                });
            } catch (IOException e) {
                InfraFail.fail("IO exception during undeploying Kafka resource", e);
            }
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("strimzi.io/name", "my-cluster-zookeeper"))
            && OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("strimzi.io/name", "my-cluster-kafka"))
            && OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("strimzi.io/name", "my-cluster-entity-operator"));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.getAnyPod("name", "strimzi-cluster-operator").isPresent();
    }

    public void addAccounts() {
        final String brokersNameBase = "my-cluster-kafka";

        Account kafka = new Account();
        Map<String, String> kafkaParameters = new HashMap<>();
        kafkaParameters.put("brokers", brokersNameBase + "-brokers:9092");
        kafka.setService("kafka");
        kafka.setProperties(kafkaParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka", kafka);

        //for ui testing - plain:
        Account kafkaAutodetectPlain = new Account();
        Map<String, String> kafkaAutodetectPlainParameters = new HashMap<>();
        kafkaAutodetectPlainParameters.put("brokers", constructBrokerName(brokersNameBase, 9092));
        kafkaAutodetectPlainParameters.put("transportprotocol", "PLAIN");
        kafkaAutodetectPlain.setService("kafka-autodetect-plain");
        kafkaAutodetectPlain.setProperties(kafkaAutodetectPlainParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka-autodetect-plain", kafkaAutodetectPlain);

        //for ui testing - tls:
        Account kafkaAutodetectTls = new Account();
        Map<String, String> kafkaAutodetectTlsParameters = new HashMap<>();
        kafkaAutodetectTlsParameters.put("brokers", constructBrokerName(brokersNameBase, 9093));
        kafkaAutodetectTlsParameters.put("transportprotocol", "TLS");
        kafkaAutodetectTls.setService("kafka-autodetect-tls");
        kafkaAutodetectTls.setProperties(kafkaAutodetectTlsParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka-autodetect-tls", kafkaAutodetectTls);
    }

    private static String constructBrokerName(String brokersNameBase, Integer port) {
        StringBuilder brokersGeneratedName = new StringBuilder();
        brokersGeneratedName.append(brokersNameBase);
        brokersGeneratedName.append("-bootstrap.");
        brokersGeneratedName.append(OpenShiftUtils.getInstance().getNamespace());
        brokersGeneratedName.append(".svc:");
        brokersGeneratedName.append(port.toString());
        return brokersGeneratedName.toString();
    }
}
