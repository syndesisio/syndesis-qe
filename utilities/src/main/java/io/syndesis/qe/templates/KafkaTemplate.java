package io.syndesis.qe.templates;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaTemplate {
    private static final String KAFKA_RESOURCES = Paths.get("../utilities/src/main/resources/kafka/strimzi-cluster-operator-0.8.2.yaml").toAbsolutePath().toString();
    private static final String KAFKA_DEPLOYMENT = Paths.get("../utilities/src/main/resources/kafka/strimzi-deployment.yaml").toAbsolutePath().toString();
    private static final String KAFKA_CR = "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.8.2/examples/kafka/kafka-ephemeral.yaml";

    public static void deploy() {
        if (!TestUtils.isUserAdmin()) {
            log.error("*************************************************");
            log.error("* Kafka deployment needs user with admin rights *");
            log.error("*************************************************");
            log.error("If you are using minishift, you can use \"oc adm policy --as system:admin add-cluster-role-to-user cluster-admin developer\"");
            throw new RuntimeException("Kafka deployment needs user with admin rights!");
        }

        List<String> resources = new ArrayList<>();
        resources.add(KAFKA_RESOURCES);
        resources.add(KAFKA_DEPLOYMENT);

        for (String resource : resources) {
            log.info("Creating " + resource);
            OpenShiftUtils.create(resource);
        }

        try (InputStream is = new URL(KAFKA_CR).openStream()) {
            CustomResourceDefinition crd = OpenShiftUtils.client().customResourceDefinitions().load(is).get();
            Map<String, Object> kafka = (Map)crd.getSpec().getAdditionalProperties().get("kafka");
            kafka.put("replicas", 1);
            ((Map)kafka.get("config")).put("offsets.topic.replication.factor", 1);
            ((Map)kafka.get("config")).put("transaction.state.log.replication.factor", 1);
            ((Map)kafka.get("config")).put("transaction.state.log.min.isr", 1);
            Map<String, Object> zookeeper = (Map)crd.getSpec().getAdditionalProperties().get("zookeeper");
            zookeeper.put("replicas", 1);

            OpenShiftUtils.invokeApi(
                    HttpUtils.Method.POST,
                    "/apis/kafka.strimzi.io/v1alpha1/namespaces/" + TestConfiguration.openShiftNamespace() + "/kafkas",
                    Serialization.jsonMapper().writeValueAsString(crd)
            );
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to create kafka custom resource", ex);
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

    private static void addAccount() {
        Account kafka = new Account();
        Map<String, String> kafkaParameters = new HashMap<>();
        kafkaParameters.put("brokers", "my-cluster-kafka-brokers:9092");
        kafka.setService("kafka");
        kafka.setProperties(kafkaParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka", kafka);
    }
}
