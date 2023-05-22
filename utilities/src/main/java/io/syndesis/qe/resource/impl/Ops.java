package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.addon.Addon;
import io.syndesis.qe.endpoint.client.EndpointClient;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.junit.Assert;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Streams;
import com.google.common.io.Files;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ops implements Resource {

    private static final String NAMESPACE = "application-monitoring";
    private static final String TMP_FOLDER = "/tmp/application-monitoring-operator";
    private static final String CRB_NAME = "application-monitoring-anonymous-view";

    private CustomResourceDefinitionContext getApplicationMonitoringContext() {
        return getGenericContext(
            OpenShiftUtils.getInstance().apiextensions().v1beta1().customResourceDefinitions()
                .withName("applicationmonitorings.applicationmonitoring.integreatly.org").get()
        );
    }

    private CustomResourceDefinitionContext getGenericContext(CustomResourceDefinition crd) {
        CustomResourceDefinitionContext.Builder builder = new CustomResourceDefinitionContext.Builder()
            .withGroup(crd.getSpec().getGroup())
            .withPlural(crd.getSpec().getNames().getPlural())
            .withScope(crd.getSpec().getScope())
            .withVersion(crd.getSpec().getVersion());
        return builder.build();
    }

    private NamespacedOpenShiftClient getMonitoringNamespace() {
        return OpenShiftUtils.getInstance().inNamespace(NAMESPACE);
    }

    /*
     *  application monitoring gets stuck when deleting a namespace when a finalizer is present in the metadata
     */
    private void finalizerWorkaround() {
        RawCustomResourceOperationsImpl crClients = OpenShiftUtils.getInstance().customResource(getApplicationMonitoringContext());
        List<Object> items = (List<Object>) crClients.list().get("items");
        items.forEach(item -> {
            try {
                Map<String, Object> cr = (Map<String, Object>) item;
                Map<String, Object> metadata = (Map<String, Object>) cr.get("metadata");
                String name = (String) metadata.get("name");
                String namespace = (String) metadata.get("namespace");
                metadata.remove("finalizers");
                crClients.edit(namespace, name, cr);
            } catch (Exception e) {
                log.error("Could not edit application monitoring CRs", e);
            }
        });
    }

    private void ensureApplicationMonitoringIsPulled() throws IOException, InterruptedException {
        if (!new File(TMP_FOLDER).exists()) {
            Process p;
            p = new ProcessBuilder()
                .directory(new File("/tmp"))
                .redirectOutput(new File("/tmp/gitout"))
                .command("git", "clone", "--depth", "1", "--branch", TestConfiguration.appMonitoringVersion(),
                    "https://github.com/integr8ly/application-monitoring-operator")
                .start();
            p.waitFor();
            //Use oc used by the testsuite
            Path scriptPath = Paths.get(TMP_FOLDER, "scripts", "install.sh");
            String script = IOUtils.toString(Files.newReader(scriptPath.toFile(), Charset.defaultCharset()));
            String ocReplaced = script.replaceAll("oc", OpenShifts.getBinaryPath());
            Files.write(ocReplaced.getBytes(Charset.defaultCharset()), scriptPath.toFile());
        }
    }

    @Override
    public void deploy() {
        final NamespacedOpenShiftClient ocp = getMonitoringNamespace();
        log.info("Installing monitoring applications");
        try {
            ensureApplicationMonitoringIsPulled();
        } catch (InterruptedException | IOException e) {
            log.error("Git pull failed", e);
        }
        if (!isDeployed()) {
            log.info("Ops is already deployed");
            try {
                Process p;
                //this needs to be a process to create the required configs for the oc binary
                p = new ProcessBuilder()
                    .command(OpenShifts.getBinaryPath(), "login", "-u=" + TestConfiguration.adminUsername(),
                        "-p=" + TestConfiguration.adminPassword())
                    .start();
                p.waitFor();
                p = new ProcessBuilder()
                    .directory(new File(TMP_FOLDER))
                    .redirectOutput(new File("/tmp/out"))
                    .command("make", "cluster/install")
                    .start();
                Process finalP = p;
                TestUtils.waitFor(() -> !finalP.isAlive(), 1, 3 * 60, "Application monitoring operator installation didn't finish in time");
                int ret = p.exitValue();
                log.info("Installation finished with return code {}", ret);
                if (ret != 0) {
                    log.error("Installation process finished with code {}", ret);
                    InfraFail.fail("Application monitoring stack installation failed with following log: {}",
                        IOUtils.toString(new URL("/tmp/out").openStream(), Charset.defaultCharset()));
                }
                OpenShiftWaitUtils.waitFor(() -> ocp.routes().list().getItems().size() >= 3);
            } catch (IOException | InterruptedException | TimeoutException e) {
                log.error("Monitoring applications deployment failed", e);
                Assert.fail("Monitoring applications deployment failed \n" + e.getMessage());
            }
        }
        OpenShiftUtils.getInstance().rbac().clusterRoleBindings().createOrReplace(new ClusterRoleBindingBuilder()
            .withNewMetadata()
            .withName(CRB_NAME)
            .endMetadata()
            .addNewSubject()
            .withName("system:anonymous")
            .withKind("User")
            .endSubject()
            .withNewRoleRef()
            .withName("view")
            .withKind("ClusterRole")
            .endRoleRef()
            .build());
        OpenShiftUtils.getInstance().namespaces().withName(TestConfiguration.openShiftNamespace()).edit(
            namespace -> new NamespaceBuilder(namespace)
                .editMetadata()
                .addToLabels("monitoring-key", "middleware")
                .endMetadata()
                .build());

        finalizerWorkaround();
        try {
            OpenShiftWaitUtils.waitFor(() -> ocp.pods().list().getItems().size() > 4);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Waiting for installation of application monitoring failed", e);
        }
        ResourceFactory.get(Syndesis.class).updateAddon(Addon.OPS, true);
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().namespaces().withName(TestConfiguration.openShiftNamespace()).edit(
            namespace -> new NamespaceBuilder(namespace)
                .editMetadata()
                .removeFromLabels("monitoring-key")
                .endMetadata()
                .build());

        OpenShiftUtils.getInstance().clusterRoleBindings().withName(CRB_NAME).delete();
        if (!OpenShiftWaitUtils.isAPodReady("syndesis.io/component", "operator").getAsBoolean()) {
            OpenShiftUtils.scale("syndesis-operator", 1, OpenShiftUtils.ResourceType.DEPLOYMENT);
            OpenShiftUtils.scale("syndesis-db", 1, OpenShiftUtils.ResourceType.DEPLOYMENT_CONFIG);
        }
        if (ResourceFactory.get(Syndesis.class).isAddonEnabled(Addon.OPS)) {
            ResourceFactory.get(Syndesis.class).updateAddon(Addon.OPS, false);
        }
        try {
            Process p = new ProcessBuilder()
                .directory(new File(TMP_FOLDER))
                .command("make", "cluster/clean")
                .start();
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new IllegalStateException(IOUtils.toString(p.getErrorStream(), Charset.defaultCharset())
                    + "\n" + IOUtils.toString(p.getInputStream(), Charset.defaultCharset()));
            }
        } catch (IOException | InterruptedException | IllegalStateException e) {
            log.error("Something went wrong with cleaning up after the application-monitoring stack", e);
        }
    }

    private JsonNode getTargets() {
        Invocation.Builder invocation = EndpointClient.getClient().target(getPrometheusRoute()).path("api").path("v1").path("targets")
            .property("disable-logging", true)
            .request(MediaType.APPLICATION_JSON);
        return invocation.get(JsonNode.class);
    }

    @Override
    public boolean isReady() {
        final NamespacedOpenShiftClient ocp = getMonitoringNamespace();
        boolean podsDeployed =
            ocp.pods().list().getItems().stream().allMatch(p -> p.getStatus().getContainerStatuses().stream().allMatch(ContainerStatus::getReady));
        boolean targetsDiscovered = Streams.stream(getTargets().get("data").get("activeTargets").elements())
            .filter(node -> node.get("labels").get("namespace").asText().equals(TestConfiguration.openShiftNamespace()))
            .map(node -> node.get("labels").get("service").asText())
            .collect(Collectors.toList()).containsAll(Arrays.asList("syndesis-server", "syndesis-db", "syndesis-operator-metrics", "syndesis-meta"));
        return podsDeployed && targetsDiscovered;
    }

    @Override
    public boolean isDeployed() {
        return getMonitoringNamespace().pods().list().getItems().size() > 0;
    }

    public String getPrometheusRoute() {
        return "https://" + getMonitoringNamespace().routes().withName("prometheus-route").get().getSpec().getHost();
    }
}
