package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kafka implements Resource {
    private static final String KAFKA_CRDS = Paths.get("../utilities/src/main/resources/kafka/kafka-crds.yaml").toAbsolutePath().toString();
    private static final String KAFKA_RESOURCES = Paths.get("../utilities/src/main/resources/kafka/strimzi-deployment.yaml")
        .toAbsolutePath().toString();
    private static final String KAFKA_CR = Paths.get("../utilities/src/main/resources/kafka/kafka-ephemeral.yaml").toAbsolutePath().toString();

    @Override
    public void deploy() {
        // Replace namespace in the resources
        TestUtils.replaceInFile(Paths.get(KAFKA_RESOURCES).toFile(), "\\$NAMESPACE\\$", TestConfiguration.openShiftNamespace());

        for (String resource : Arrays.asList(KAFKA_CRDS, KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Creating " + resource);
            OpenShiftUtils.create(resource);
        }
        addAccounts();
    }

    @Override
    public void undeploy() {
        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Deleting " + resource);
            OpenShiftUtils.delete(resource);
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("statefulset.kubernetes.io/pod-name", "my-cluster-kafka-0"))
            && OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("statefulset.kubernetes.io/pod-name", "my-cluster-zookeeper-0"))
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
