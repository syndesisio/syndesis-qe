package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
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
    private static final String KAFKA_RESOURCES = Paths.get("../utilities/src/main/resources/kafka/strimzi-deployment.yaml")
        .toAbsolutePath().toString();
    private static final String KAFKA_CR = Paths.get("../utilities/src/main/resources/kafka/kafka-ephemeral.yaml").toAbsolutePath().toString();

    @Override
    public void deploy() {
        // Replace namespace in the resources
        TestUtils.replaceInFile(Paths.get(KAFKA_RESOURCES).toFile(), "\\$NAMESPACE\\$", TestConfiguration.openShiftNamespace());

        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Creating " + resource);
            OpenShiftUtils.create(resource);
        }

        addAccount();
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
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("statefulset.kubernetes.io/pod-name", "my-cluster-kafka-0"));
    }

    private static void addAccount() {
        Account kafka = new Account();
        Map<String, String> kafkaParameters = new HashMap<>();
        kafkaParameters.put("brokers", "my-cluster-kafka-brokers:9092");
        kafka.setService("kafka");
        kafka.setProperties(kafkaParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka", kafka);
    }
}
