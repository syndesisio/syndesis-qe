package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPEndpoints implements Resource {
    public static final String KEYSTORE_SECRET_NAME = "httpendpoints-secret";
    public static final String KEYSTORE_URL =
        "https://github.com/syndesisio/syndesis-qe-HTTPEndpoints/blob/master/src/main/resources/keystore.p12?raw=true";
    private static final String LABEL_NAME = "app";
    private static final String APP_NAME = "httpendpoints";
    private static final String IMAGE = "quay.io/syndesis_qe/httpendpoints";
    private static final String HTTP_SERVICE_NAME = "http-svc";
    private static final String HTTPS_SERVICE_NAME = "https-svc";

    @Override
    public void deploy() {
        if (!isDeployed()) {
            Map<String, String> labels = TestUtils.map(LABEL_NAME, APP_NAME);
            List<ContainerPort> ports = new ArrayList<>();
            ports.add(new ContainerPortBuilder().withName("http").withContainerPort(8080).build());
            ports.add(new ContainerPortBuilder().withName("https").withContainerPort(8443).build());
            //@formatter:off
            OpenShiftUtils.getInstance().deploymentConfigs().createOrReplace(new DeploymentConfigBuilder()
                .withNewMetadata()
                    .withName(APP_NAME)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withSelector(labels)
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(labels)
                        .endMetadata()
                        .withNewSpec()
                            .withContainers(new ContainerBuilder().withName(APP_NAME).withPorts(ports).withImage(IMAGE).build())
                        .endSpec()
                    .endTemplate()
                    .addNewTrigger()
                        .withType("ConfigChange")
                    .endTrigger()
                .endSpec()
            .build());

            OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
                .withNewMetadata()
                    .withName(HTTP_SERVICE_NAME)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withPorts(getServicePort(HTTP_SERVICE_NAME, 8080))
                    .withSelector(labels)
                .endSpec()
            .build());

            OpenShiftUtils.getInstance().services().createOrReplace(new ServiceBuilder()
                .withNewMetadata()
                .withName(HTTPS_SERVICE_NAME)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withPorts(getServicePort(HTTPS_SERVICE_NAME, 8443))
                .withSelector(labels)
                .endSpec()
                .build());

            String content = "";
            // Download the keystore and base64 encode it
            try {
                content = new String(Base64.getEncoder().encode(IOUtils.toByteArray(new URL(KEYSTORE_URL))));
            } catch (IOException e) {
                fail("Unable to read " + KEYSTORE_URL, e);
            }

            OpenShiftUtils.getInstance().secrets().createOrReplace(new SecretBuilder()
                .withNewMetadata()
                    .withName(KEYSTORE_SECRET_NAME)
                .endMetadata()
                .addToData("keystore.p12", content)
                .build());
            //@formatter:on
        }
        addAccounts();
    }

    private ServicePort getServicePort(String name, int port) {
        return new ServicePortBuilder().withName(name).withPort(port).withProtocol("TCP")
            .withTargetPort(new IntOrString(port)).build();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().secrets().withName(KEYSTORE_SECRET_NAME).delete();
        OpenShiftUtils.getInstance().deploymentConfigs().withName(APP_NAME).cascading(true).delete();
        OpenShiftUtils.getInstance().imageStreams().withName(APP_NAME).delete();
        OpenShiftUtils.getInstance().services().list().getItems().stream()
            .filter(s -> s.getMetadata().getName().endsWith("-svc"))
            .forEach(s -> OpenShiftUtils.getInstance().services().delete(s));
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod(LABEL_NAME, APP_NAME));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.isDcDeployed(APP_NAME);
    }

    public void addAccounts() {
        Account http = new Account();
        Map<String, String> params = new HashMap<>();
        params.put("baseUrl", "http://http-svc:8080");
        http.setService("http");
        http.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("http", http);
        Account https = new Account();
        params = new HashMap<>();
        params.put("baseUrl", "https://https-svc:8443");
        https.setService("https");
        https.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("https", https);
    }
}
