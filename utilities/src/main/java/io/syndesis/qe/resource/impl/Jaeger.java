package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Subject;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Jaeger implements Resource {
    private static final String[] JAEGER_RESOURCES = new String[] {
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/crds/jaegertracing_v1_jaeger_crd.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/service_account.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role_binding.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/operator.yaml"
    };

    private List<HasMetadata> processedResources = new ArrayList<>();

    @Override
    public void deploy() {
        processResources();
        log.info("Creating Jaeger resources");

        processedResources.forEach(res -> {
            try {
                OpenShiftUtils.getInstance().resource(res).createOrReplace();
            } catch (KubernetesClientException ex) {
                // When the CRD for jaeger already exists, the request fails with "Invalid value: 0x0: must be specified for an update.", so ignore it
                if (!ex.getMessage().contains("an update")) {
                    throw ex;
                }
            }
        });

        OpenShiftUtils.getInstance().roleBindings().createOrReplaceWithNew()
            .withNewMetadata().withName("syndesis-jaeger-operator").endMetadata()
            .withNewRoleRef().withName("jaeger-operator").endRoleRef()
            .withSubjects(new ObjectReferenceBuilder().withKind("ServiceAccount").withName("syndesis-operator").withNamespace(TestConfiguration.openShiftNamespace()).build())
            .done();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().resourceList(processedResources).delete();
        OpenShiftUtils.getInstance().apps().replicaSets().withLabel("name", "jaeger-operator").delete();
        OpenShiftUtils.getInstance().deletePods("name", "jaeger-operator");
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
