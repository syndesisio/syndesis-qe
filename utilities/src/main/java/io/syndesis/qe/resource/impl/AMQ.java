package io.syndesis.qe.resource.impl;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AMQ implements Resource {
    private static final String NAME = "syndesis-amq";

    @Override
    public void deploy() {
        if (!TestUtils.isDcDeployed(NAME)) {
            Template template;
            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-amq.yml")) {
                template = OpenShiftUtils.getInstance().templates().load(is).get();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read template ", ex);
            }

            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("MQ_USERNAME", "amq");
            templateParams.put("MQ_PASSWORD", "topSecret");

            OpenShiftUtils.getInstance().templates().withName("syndesis-amq").delete();

            KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);

            OpenShiftUtils.getInstance().createResources(processedTemplate);

            if (OpenShiftUtils.getInstance().getServices().stream().noneMatch(service -> "broker-amq-tcp".equals(service.getMetadata
                ().getName()))) {
                OpenShiftUtils.getInstance().createService(OpenShiftUtils.getInstance().services()
                    .load(Paths.get("../utilities/src/main/resources/templates/syndesis-default-amq-service.yml").toFile()).get());
            }
        }
        //this is not part of deployment, but let's have it the same method:
        AMQ.addAccounts();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> NAME.equals(dc.getMetadata().getName())).findFirst()
            .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream()
            .filter(service -> "syndesis-amq".equals(service.getMetadata().getLabels().get("template"))).findFirst()
            .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getImageStreams().stream().filter(is -> "syndesis-amq-63".equals(is.getMetadata().getName())).findFirst()
            .ifPresent(is -> OpenShiftUtils.getInstance().deleteImageStream(is));
        OpenShiftUtils.getInstance().getServices().stream().filter(se -> "broker-amq-tcp".equals(se.getMetadata().getName())).findFirst()
            .ifPresent(se -> OpenShiftUtils.getInstance().deleteService(se));
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("application", NAME));
    }

    private static void addAccounts() {

        Account openwireAccount = new Account();
        Map<String, String> openwireAccountParameters = new HashMap<>();
        openwireAccountParameters.put("username", "amq");
        openwireAccountParameters.put("password", "topSecret");
        // For PROD build, the default broker-amq-tcp service is used, for upstream build, our service broker-amq-tcp `syndesis-default-amq-service
        // .yml` is used.
        openwireAccountParameters.put("brokerUrl", "tcp://broker-amq-tcp:61616");
        openwireAccount.setService("amq");
        openwireAccount.setProperties(openwireAccountParameters);

        Account amqpAccount = new Account();
        Map<String, String> amqpAccountParameters = new HashMap<>();
        amqpAccountParameters.put("username", "amq");
        amqpAccountParameters.put("password", "topSecret");
        amqpAccountParameters.put("connectionUri", "amqp://syndesis-amq-tcp:5672");
        amqpAccountParameters.put("clientID", UUID.randomUUID().toString());
        amqpAccountParameters.put("skipCertificateCheck", "Disable");
        amqpAccountParameters.put("brokerCertificate", "");
        amqpAccountParameters.put("clientCertificate", "");
        amqpAccount.setService("amqp");
        amqpAccount.setProperties(amqpAccountParameters);

        Account mqttAccount = new Account();
        Map<String, String> mqttAccountParameters = new HashMap<>();
        mqttAccountParameters.put("userName", "amq");
        mqttAccountParameters.put("password", "topSecret");
        mqttAccountParameters.put("brokerUrl", "tcp://syndesis-amq-tcp:1883");

        mqttAccount.setService("MQTT");
        mqttAccount.setProperties(mqttAccountParameters);

        AccountsDirectory.getInstance().addAccount("AMQ", openwireAccount);
        AccountsDirectory.getInstance().addAccount("AMQP", amqpAccount);
        AccountsDirectory.getInstance().addAccount("QE MQTT", mqttAccount);
    }
}
