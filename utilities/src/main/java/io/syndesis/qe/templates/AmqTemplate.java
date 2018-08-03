package io.syndesis.qe.templates;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.Template;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmqTemplate {

    public static void deploy() {
        if (!TestUtils.isDcDeployed("broker-amq")) {
            Template template;
            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-amq.yml")) {
                template = OpenShiftUtils.client().templates().load(is).get();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read template ", ex);
            }

            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("MQ_USERNAME", "amq");
            templateParams.put("MQ_PASSWORD", "topSecret");

            // try to delete previous broker
            cleanUp();
            OpenShiftUtils.client().templates().withName("syndesis-amq").delete();

            KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);

            OpenShiftUtils.getInstance().createResources(processedTemplate);

            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("application", "broker"));
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for broker failed ", e);
            }
        }
        //this is not part of deployment, but let's have it the same method:
        AmqTemplate.addAccounts();
    }

    public static void cleanUp() {
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> "broker-amq".equals(dc.getMetadata().getName())).findFirst()
                .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> "syndesis-amq".equals(service.getMetadata().getLabels().get("template"))).findFirst()
                .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getImageStreams().stream().filter(is -> "jboss-amq-63".equals(is.getMetadata().getName())).findFirst()
                .ifPresent(is -> OpenShiftUtils.getInstance().deleteImageStream(is));
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private static void addAccounts() {

        Account openwireAccount = new Account();
        Map<String, String> openwireAccountParameters = new HashMap<>();
        openwireAccountParameters.put("username", "amq");
        openwireAccountParameters.put("password", "topSecret");
        openwireAccountParameters.put("brokerUrl", "tcp://broker-amq:61616");
        openwireAccount.setService("amq");
        openwireAccount.setProperties(openwireAccountParameters);

        Account amqpAccount = new Account();
        Map<String, String> amqpAccountParameters = new HashMap<>();
        amqpAccountParameters.put("username", "amq");
        amqpAccountParameters.put("password", "topSecret");
        amqpAccountParameters.put("connectionUri", "amqp://broker-amq:5672");
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
        mqttAccountParameters.put("brokerUrl", "tcp://broker-amq:1883");

        mqttAccount.setService("MQTT");
        mqttAccount.setProperties(mqttAccountParameters);

        AccountsDirectory.getInstance().addAccount("AMQ", openwireAccount);
        AccountsDirectory.getInstance().addAccount("AMQP", amqpAccount);
        AccountsDirectory.getInstance().addAccount("QE MQTT", mqttAccount);
    }
}
