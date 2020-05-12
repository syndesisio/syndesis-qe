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

import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Kafka implements Resource {
    private static final String KAFKA_RESOURCES = Paths.get("../utilities/src/main/resources/kafka/strimzi-deployment.yaml")
        .toAbsolutePath().toString();
    private static final String KAFKA_CR = Paths.get("../utilities/src/main/resources/kafka/kafka-ephemeral.yaml").toAbsolutePath().toString();
    private static final String OCP_KAFKA_VIEW_ROLE = "kafkas.kafka.strimzi.io-view";
    private static final String OCP_SERVICE_ACCOUNT = "syndesis-server";

    @Override
    public void deploy() {

        // Replace namespace in the resources
        TestUtils.replaceInFile(Paths.get(KAFKA_RESOURCES).toFile(), "\\$NAMESPACE\\$", TestConfiguration.openShiftNamespace());

        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Creating " + resource);
            OpenShiftUtils.create(resource);
        }

        addClusterRole();

        addAccounts();
    }

    @Override
    public void undeploy() {

        deleteClusterRole();

        for (String resource : Arrays.asList(KAFKA_RESOURCES, KAFKA_CR)) {
            log.info("Deleting " + resource);
            OpenShiftUtils.delete(resource);
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("statefulset.kubernetes.io/pod-name", "my-cluster-kafka-0"));
    }

    public static void addAccounts() {
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

    private static void addClusterRole() {
        OpenShiftUtils.getInstance().rbac().clusterRoles().createOrReplaceWithNew()
            .withNewMetadata()
            .withName(OCP_KAFKA_VIEW_ROLE)
            .endMetadata()
            .addNewRule()
            .addToApiGroups("apiextensions.k8s.io")
            .addToResources("customresourcedefinitions")
            .addToVerbs("get", "list")
            .endRule()
            .addNewRule()
            .addToApiGroups("kafka.strimzi.io")
            .addToResources("kafkas")
            .addToVerbs("get", "list")
            .endRule()
            .done();

        //create new clusterrolebinding to our service account:
        OpenShiftUtils.getInstance().rbac().clusterRoleBindings().createOrReplaceWithNew()
            .withNewMetadata()
            .withName(OCP_KAFKA_VIEW_ROLE)
            .endMetadata()
            .withNewRoleRef()
            .withName(OCP_KAFKA_VIEW_ROLE)
            .withKind("ClusterRole")
            .endRoleRef()
            .addNewSubject()
            .withKind("ServiceAccount").withName(OCP_SERVICE_ACCOUNT).withNamespace(OpenShiftUtils.getInstance().getNamespace())
            .endSubject()
            .done();
    }

    private static void deleteClusterRole() {
        final ClusterRoleBinding kafkaCrb = OpenShiftUtils.getInstance().rbac().clusterRoleBindings().withName(OCP_KAFKA_VIEW_ROLE).get();
        OpenShiftUtils.getInstance().rbac().clusterRoleBindings().delete(kafkaCrb);
        final ClusterRole kafkaCr = OpenShiftUtils.getInstance().rbac().clusterRoles().withName(OCP_KAFKA_VIEW_ROLE).get();
        OpenShiftUtils.getInstance().rbac().clusterRoles().delete(kafkaCr);
    }
}
