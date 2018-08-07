package io.syndesis.qe.templates;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.Template;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaTemplate {
    private static final String[] KAFKA_STRIMZI_RESOURCES = new String[] {
            "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.4.0/examples/install/cluster-operator/01-service-account.yaml",
            "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.4.0/examples/install/cluster-operator/02-role.yaml",
            "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.4.0/examples/install/cluster-operator/03-role-binding.yaml",
            "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.4.0/examples/install/cluster-operator/04-deployment.yaml",
    };

    private static final String KAFKA_TEMPLATE =
            "https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.4.0/examples/templates/cluster-operator/ephemeral-template.yaml";

    public static void deploy() {
        if (!TestUtils.isUserAdmin()) {
            log.error("*************************************************");
            log.error("* Kafka deployment needs user with admin rights *");
            log.error("*************************************************");
            log.error("If you are using minishift, you can use \"oc adm policy --as system:admin add-cluster-role-to-user cluster-admin developer\"");
            throw new RuntimeException("Kafka deployment needs user with admin rights!");
        }
        for (String resource : KAFKA_STRIMZI_RESOURCES) {
            try (InputStream is = new URL(resource).openStream()) {
                List<HasMetadata> list = OpenShiftUtils.client().load(is).get();
                for (HasMetadata hasMetadata : list) {
                    OpenShiftUtils.create(hasMetadata.getKind(), hasMetadata);
                }
            } catch (IOException ex) {
                fail("Unable to process " + resource, ex);
            }
        }

        Template template;
        try (InputStream is = new URL(KAFKA_TEMPLATE).openStream()) {
            template = OpenShiftUtils.client().templates().load(is).get();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read template ", ex);
        }

        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("KAFKA_NODE_COUNT", "1");
        templateParams.put("ZOOKEEPER_NODE_COUNT", "1");
        templateParams.put("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
        templateParams.put("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");

        OpenShiftUtils.client().templates().withName("strimzi-ephemeral").delete();

        KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);

        OpenShiftUtils.getInstance().createResources(processedTemplate);

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
        kafkaParameters.put("brokerUrl", "my-cluster-kafka:9092");
        kafka.setService("kafka");
        kafka.setProperties(kafkaParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka", kafka);
    }
}
