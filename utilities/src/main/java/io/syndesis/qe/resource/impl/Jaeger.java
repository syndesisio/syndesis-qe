package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Subject;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.RoleBindingBuilder;
import io.fabric8.openshift.api.model.RouteBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * External Jaeger instance. Make sure you don't have syndesis-jaeger in the namespace
 */
@Slf4j
public class Jaeger implements Resource {

    private static final String VERSION_FOR_OCP3 = "1.17.0";
    private static final String[] JAEGER_RESOURCES = new String[] {
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/crds/jaegertracing.io_jaegers_crd.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/service_account.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/role_binding.yaml",
        "https://raw.githubusercontent.com/jaegertracing/jaeger-operator/%s/deploy/operator.yaml"
    };

    private List<HasMetadata> processedResources;

    @Getter
    private String queryServiceHost;

    @Getter
    private String collectorServiceHost;

    private static final String COLLECTOR_SERVICE_NAME = "noauth-jaeger-collector";
    private static final String QUERY_SERVICE_NAME = "noauth-jaeger-query";

    @Override
    public void deploy() {
        processedResources = new ArrayList<>();
        processResources();
        log.info("Creating Jaeger resources");

        processedResources.forEach(res -> {
            try {
                // TODO problem with creation CRD on our clusters (stream was reset: NO_ERROR), skip, the CRD is there after the first syndesis
                //  install anyway
                if (!(res instanceof CustomResourceDefinition ||
                    res instanceof io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinitionVersion)) {
                    OpenShiftUtils.getInstance().resource(res).createOrReplace();
                }
            } catch (KubernetesClientException ex) {
                // When the CRD for jaeger already exists, the request fails with "Invalid value: 0x0: must be specified for an update.",
                // so ignore it
                if (!ex.getMessage().contains("an update")) {
                    throw ex;
                }
            }
        });

        OpenShiftUtils.getInstance().roleBindings().createOrReplace(new RoleBindingBuilder()
            .withNewMetadata().withName("jaeger-operator-cluster").endMetadata()
            .withNewRoleRef().withName("jaeger-operator-cluster").endRoleRef()
            .withSubjects(new ObjectReferenceBuilder().withKind("ServiceAccount").withName("jaeger-operator")
                .withNamespace(TestConfiguration.openShiftNamespace()).build())
            .build());

        OpenShiftWaitUtils.waitUntilPodIsRunning("jaeger-operator");
        OpenShiftUtils.getInstance().pods()
            .withName(OpenShiftUtils.getPodByPartialName("jaeger-operator").get().getMetadata().getName())
            .edit(
                pod -> new PodBuilder(pod)
                    .editMetadata()
                    .addToLabels("syndesis.io/component", "jaeger-operator")
                    .endMetadata().build());
        createJaegerCr("jaeger-external");
        OpenShiftWaitUtils.waitUntilPodIsRunning("jaeger-all-in-one");

        OpenShiftUtils.getInstance().createService(OpenShiftUtils.getInstance().services()
            .load(Paths.get("src/test/resources/jaeger/jaeger-service-collector.yml").toFile()).get());
        collectorServiceHost = OpenShiftUtils.getInstance().routes().createOrReplace(new RouteBuilder()
            .withNewMetadata()
            .withName(COLLECTOR_SERVICE_NAME)
            .endMetadata()
            .withNewSpec()
            .withWildcardPolicy("None")
            .withNewTo()
            .withKind("Service").withName(COLLECTOR_SERVICE_NAME)
            .endTo()
            .endSpec()
            .build()).getSpec().getHost();

        OpenShiftUtils.getInstance().createService(OpenShiftUtils.getInstance().services()
            .load(Paths.get("src/test/resources/jaeger/jaeger-service-query.yml").toFile()).get());
        queryServiceHost = OpenShiftUtils.getInstance().routes().createOrReplace(new RouteBuilder()
            .withNewMetadata()
            .withName(QUERY_SERVICE_NAME)
            .endMetadata()
            .withNewSpec()
            .withWildcardPolicy("None")
            .withNewTo()
            .withKind("Service").withName(QUERY_SERVICE_NAME)
            .endTo()
            .endSpec()
            .build()).getSpec().getHost();
    }

    @Override
    public void undeploy() {
        // Don't delete clusterwide resources
        OpenShiftUtils.getInstance().resourceList(processedResources.stream()
                .filter(res -> !(res instanceof CustomResourceDefinition ||
                    res instanceof io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinitionVersion || res instanceof ClusterRole))
                .collect(Collectors.toList()))
            .cascading(true).delete();

        OpenShiftUtils.getInstance().services().withName(COLLECTOR_SERVICE_NAME).delete();
        OpenShiftUtils.getInstance().services().withName(QUERY_SERVICE_NAME).delete();
        OpenShiftUtils.getInstance().routes().withName(COLLECTOR_SERVICE_NAME).delete();
        OpenShiftUtils.getInstance().routes().withName(QUERY_SERVICE_NAME).delete();
        OpenShiftUtils.binary().execute("delete", "jaeger.jaegertracing.io/jaeger-all-in-one");
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getPodByPartialName("jaeger-operator")) &&
            OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getPodByPartialName("jaeger-all-in-one"));
    }

    @Override
    public boolean isDeployed() {
        return OpenShiftUtils.getPodByPartialName("jaeger-all-in-one").isPresent();
    }

    private void processResources() {
        for (String jaegerResource : JAEGER_RESOURCES) {
            String jaegerVersion = OpenShiftUtils.isOpenshift3() ? VERSION_FOR_OCP3 : TestConfiguration.jaegerVersion();
            jaegerResource = String.format(jaegerResource, "v" + jaegerVersion); // version 1.20.0 ==> repo v1.20.0
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

                    // change docker image to quay
                    if (resource instanceof Deployment) {
                        ((Deployment) resource).getSpec().getTemplate().getSpec().getContainers().get(0)
                            .setImage("quay.io/jaegertracing/jaeger-operator:" + jaegerVersion);
                    }
                }
            } catch (IOException e) {
                fail("Unable to process Jaeger resource " + jaegerResource, e);
            }
        }
    }

    /**
     * Create Jaeger cr from /resources/jaeger/cr
     *
     * @param crName name of cr file
     */
    public static void createJaegerCr(String crName) {
        if (OpenShiftUtils.isOpenshift3()) {
            TestUtils.replaceInFile(new File("src/test/resources/jaeger/cr/" + crName + ".yml"), "REPLACE_IMAGE",
                "docker.io/jaegertracing/all-in-one:" + VERSION_FOR_OCP3);
        } else {
            TestUtils.replaceInFile(new File("src/test/resources/jaeger/cr/" + crName + ".yml"), "REPLACE_IMAGE",
                "quay.io/jaegertracing/all-in-one:" + TestConfiguration.jaegerVersion());
        }
        OpenShiftUtils.create(Paths.get("src/test/resources/jaeger/cr/" + crName + ".yml").toAbsolutePath().toString());
    }
}
