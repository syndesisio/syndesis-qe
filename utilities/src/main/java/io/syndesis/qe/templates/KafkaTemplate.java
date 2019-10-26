package io.syndesis.qe.templates;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaTemplate {
    private static final String KAFKA_RESOURCES = Paths.get("../utilities/src/main/resources/kafka/strimzi-deployment.yaml")
        .toAbsolutePath().toString();
    private static final String KAFKA_CR = Paths.get("../utilities/src/main/resources/kafka/kafka-ephemeral.yaml").toAbsolutePath().toString();

    public static void deploy() {
        if (!TestUtils.isUserAdmin()) {
            log.error("*************************************************");
            log.error("* Kafka deployment needs user with admin rights *");
            log.error("*************************************************");
            log.error("If you are using minishift, you can use \"oc adm policy --as system:admin add-cluster-role-to-user cluster-admin developer\"");
            throw new RuntimeException("Kafka deployment needs user with admin rights!");
        }

        // Replace namespace in the resources
        TestUtils.replaceInFile(Paths.get(KAFKA_RESOURCES).toFile(), "\\$NAMESPACE\\$", TestConfiguration.openShiftNamespace());

        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Creating " + resource);
            OpenShiftUtils.create(resource);
        }

        try {
            OpenShiftWaitUtils.waitFor(
                OpenShiftWaitUtils.isAPodReady("statefulset.kubernetes.io/pod-name", "my-cluster-kafka-0"),
                12 * 60 * 1000L
            );
        } catch (InterruptedException | TimeoutException e) {
            log.error("Wait for kafka failed ", e);
        }

        addAccount();
    }

    public static void undeploy() {
        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Deleting " + resource);
            OpenShiftUtils.delete(resource);
        }
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
