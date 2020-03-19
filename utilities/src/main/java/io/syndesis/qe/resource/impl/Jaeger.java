package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Subject;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jaeger implements Resource {
    private static final String[] JAEGER_RESOURCES = new String[] {
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/crds/jaegertracing.io_jaegers_crd.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/service_account.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role_binding.yaml"
    };

    private List<HasMetadata> processedResources;

    @Override
    public void deploy() {
        processedResources = new ArrayList<>();
        processResources();
        log.info("Creating Jaeger resources");

        processedResources.forEach(res -> {
            try {
                OpenShiftUtils.getInstance().resource(res).createOrReplace();
            } catch (KubernetesClientException ex) {
                // When the CRD for jaeger already exists, the request fails with "Invalid value: 0x0: must be specified for an update.",
                // so ignore it
                if (!ex.getMessage().contains("an update")) {
                    throw ex;
                }
            }
        });

        OpenShiftUtils.getInstance().roleBindings().createOrReplaceWithNew()
            .withNewMetadata().withName("syndesis-jaeger-operator").endMetadata()
            .withNewRoleRef().withName("jaeger-operator").endRoleRef()
            .withSubjects(new ObjectReferenceBuilder().withKind("ServiceAccount").withName("syndesis-operator")
                .withNamespace(TestConfiguration.openShiftNamespace()).build())
            .done();

        // Jaeger operator is creating the syndesis-jaeger instance and therefore it is not possible to add the syndesis.io/component label
        // Do it async in a new thread so that it doesn't need to be hacked in other place where it would not make sense
        // If the wait fails, then the "wait for Syndesis to become ready" step will time out, so it is not necessary to handle the
        // exception here
        new Thread(() -> {
            try {
                OpenShiftWaitUtils.waitFor(() -> OpenShiftWaitUtils.isPodReady(
                    OpenShiftUtils.getAnyPod("app.kubernetes.io/instance", "syndesis-jaeger"))
                );
            } catch (TimeoutException | InterruptedException e) {
                log.error("Syndesis-jaeger pod never reached ready state!");
            } catch (Exception ex) {
                log.warn("Exception thrown while waiting, ignoring: ", ex);
            }
            OpenShiftUtils.getInstance().pods().withName(OpenShiftUtils.getPodByPartialName("syndesis-jaeger").get().getMetadata().getName())
                .edit().editMetadata().addToLabels("syndesis.io/component", "syndesis-jaeger").endMetadata().done();
        }).start();
    }

    @Override
    public void undeploy() {
        // Don't delete clusterwide resources
        OpenShiftUtils.getInstance().resourceList(processedResources.stream()
            .filter(res -> !(res instanceof CustomResourceDefinition || res instanceof ClusterRole))
            .collect(Collectors.toList()))
            .cascading(true).delete();
    }

    @Override
    public boolean isReady() {
        // Jaeger operator is deployed by the syndesis operator, so consider it ready always as this code runs before the CR is created
        return true;
    }

    private void processResources() {
        for (String jaegerResource : JAEGER_RESOURCES) {
            jaegerResource = String.format(jaegerResource, TestConfiguration.jaegerVersion());
            log.info("Processing " + jaegerResource);
            try (InputStream is = new URL(jaegerResource).openStream()) {
                List<HasMetadata> resources = OpenShiftUtils.getInstance().load(is).get();
                // Change the namespace in the resources to the current
                for (HasMetadata resource : resources) {
                    if (resource instanceof ClusterRoleBinding) {
                        for (Subject subject : ((ClusterRoleBinding) resource).getSubjects()) {
                            subject.setNamespace(TestConfiguration.openShiftNamespace());
                        }
                    }
                    resource.getMetadata().setNamespace(TestConfiguration.openShiftNamespace());
                    processedResources.add(resource);
                }
            } catch (IOException e) {
                fail("Unable to process Jaeger resource " + jaegerResource, e);
            }
        }
    }
}
