package io.syndesis.qe.templates;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrcTemplate {
    private static final String APP_NAME = "irc";
    private static final String LABEL_NAME = "app";

    public static void deploy() {
        if (!TestUtils.isDcDeployed("irc")) {
            List<ContainerPort> ports = new LinkedList<>();
            ports.add(new ContainerPortBuilder()
                    .withName("irc")
                    .withContainerPort(6667)
                    .withProtocol("TCP").build());

            OpenShiftUtils.client().deploymentConfigs().createOrReplaceWithNew()
                    .editOrNewMetadata()
                    .withName(APP_NAME)
                    .addToLabels(LABEL_NAME, APP_NAME)
                    .endMetadata()

                    .editOrNewSpec()
                    .addToSelector(LABEL_NAME, APP_NAME)
                    .withReplicas(1)
                    .editOrNewTemplate()
                    .editOrNewMetadata()
                    .addToLabels(LABEL_NAME, APP_NAME)
                    .endMetadata()
                    .editOrNewSpec()
                    .addNewContainer().withName(APP_NAME).withImage("syndesisqe/irc:latest").addAllToPorts(ports)

                    .endContainer()
                    .endSpec()
                    .endTemplate()
                    .addNewTrigger()
                    .withType("ConfigChange")
                    .endTrigger()
                    .endSpec()
                    .done();

            ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, APP_NAME);

            serviceSpecBuilder.addToPorts(new ServicePortBuilder()
                    .withName("irc")
                    .withPort(6667)
                    .withNodePort(31111)
                    .withTargetPort(new IntOrString(6667))
                    .build());

            OpenShiftUtils.getInstance().client().services().createOrReplaceWithNew()
                    .editOrNewMetadata()
                    .withName(APP_NAME)
                    .addToLabels(LABEL_NAME, APP_NAME)
                    .endMetadata()
                    .editOrNewSpecLike(serviceSpecBuilder.build())
                    .withType("NodePort")
                    .endSpec()
                    .done();

            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsReady(LABEL_NAME, APP_NAME, 1));
                Thread.sleep(20 * 1000);
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for {} deployment failed ", APP_NAME, e);
            }
        }
        addAccounts();
    }

    private static void addAccounts() {
        Account irc = new Account();
        Map<String, String> params = new HashMap<>();
        params.put("hostname", "irc");
        params.put("port", "6667");
        irc.setService("irc");
        irc.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("irc", irc);
    }
}
