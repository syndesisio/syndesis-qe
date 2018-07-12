package io.syndesis.qe.templates;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Template;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Response;

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
        if (!isUserAdmin()) {
            log.error("*************************************************");
            log.error("* Kafka deployment needs user with admin rights *");
            log.error("*************************************************");
            log.error("If you are using minishift, you can use \"oc adm policy --as system:admin add-cluster-role-to-user cluster-admin developer\"");
            throw new RuntimeException("Kafka deployment needs user with admin rights!");
        }
        for (String resource : KAFKA_STRIMZI_RESOURCES) {
            process(resource);
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
                    12*60*1000L
            );
        } catch (InterruptedException | TimeoutException e) {
            log.error("Wait for kafka failed ", e);
        }

        addAccount();
    }

    private static void process(String resource) {
        log.debug("Processing " + resource);

        try {
            final String content = IOUtils.toString(new URL(resource));
            String url = TestConfiguration.openShiftUrl();
            switch (getKind(content)) {
                case "serviceaccount":
                    url += "/api/v1/namespaces/" + TestConfiguration.openShiftNamespace() + "/serviceaccounts";
                    break;
                case "role":
                    url += "/apis/rbac.authorization.k8s.io/v1beta1/namespaces/" + TestConfiguration.openShiftNamespace() + "/roles";
                    break;
                case "rolebinding":
                    url += "/apis/rbac.authorization.k8s.io/v1beta1/namespaces/" + TestConfiguration.openShiftNamespace() + "/rolebindings";
                    break;
                case "clusterrole":
                    url += "/apis/rbac.authorization.k8s.io/v1beta1/clusterroles";
                    break;
                case "clusterrolebinding":
                    url += "/apis/rbac.authorization.k8s.io/v1beta1/clusterrolebindings";
                    break;
                case "deployment":
                    url += "/apis/extensions/v1beta1/namespaces/" + TestConfiguration.openShiftNamespace() + "/deployments";
                    break;
            }
            log.debug(url);
            Response response = HttpUtils.doPostRequest(
                    url,
                    content,
                    "application/yaml",
                    Headers.of("Authorization", "Bearer " + OpenShiftUtils.client().getConfiguration().getOauthToken())
            );
            log.debug("Response code: " + response.code());
            log.debug("Response: " + response.body().string());
            // 409 means that the resource already exists - this is the case for clusterrole / clusterbinding that are tied to the whole
            // cluster obviously - therefore it is ok to continue with 409
            if (response.code() != 409) {
                assertThat(response.code()).isGreaterThanOrEqualTo(200);
                assertThat(response.code()).isLessThan(300);
            }
        } catch (IOException e) {
            log.error("Unable to process file " + resource, e);
            e.printStackTrace();
        }
    }

    private static String getKind(String content) {
        final String line = content.split("\\n")[1];
        return StringUtils.substringAfter(line, "kind: ").trim().toLowerCase();
    }

    private static void addAccount() {
        Account kafka = new Account();
        Map<String, String> kafkaParameters = new HashMap<>();
        kafkaParameters.put("brokerUrl", "my-cluster-kafka:9092");
        kafka.setService("kafka");
        kafka.setProperties(kafkaParameters);
        AccountsDirectory.getInstance().getAccounts().put("kafka", kafka);
    }

    private static boolean isUserAdmin() {
        try {
            OpenShiftUtils.client().users().list();
            return true;
        } catch (KubernetesClientException ex) {
            return false;
        }
    }
}
