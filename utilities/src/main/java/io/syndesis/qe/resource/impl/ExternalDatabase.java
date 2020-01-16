package io.syndesis.qe.resource.impl;

import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;

public class ExternalDatabase implements Resource {
    private static final String NAME = "custom-postgres";

    @Override
    public void deploy() {
        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
            .withNewMetadata()
            .withName(NAME)
            .withLabels(TestUtils.map("app", NAME))
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .withNewTemplate()
            .withNewMetadata()
            .withLabels(TestUtils.map("app", NAME))
            .endMetadata()
            .withNewSpec()
            .withContainers(
                new ContainerBuilder()
                    .withEnv(
                        new EnvVar("POSTGRESQL_DATABASE", "testdb", null),
                        new EnvVar("POSTGRESQL_PASSWORD", "testpassword", null),
                        new EnvVar("POSTGRESQL_USER", "testuser", null)
                    )
                    .withPorts(
                        new ContainerPortBuilder().withContainerPort(5432).withProtocol("TCP").build()
                    )
                    .withName(NAME)
                    .withImage(" ")
                    .build()
            )
            .endSpec()
            .endTemplate()
            .withSelector(TestUtils.map("app", NAME))
            .withTriggers(
                new DeploymentTriggerPolicyBuilder()
                    .withNewImageChangeParams()
                    .withContainerNames(NAME)
                    .withFrom(new ObjectReferenceBuilder().withKind("ImageStreamTag").withName("postgresql:9.6").withNamespace("openshift").build())
                    .withAutomatic(true)
                    .endImageChangeParams()
                    .withNewType("ImageChange")
                    .build()
            )
            .endSpec().done();

        OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
            .withNewMetadata()
            .withName(NAME)
            .withLabels(TestUtils.map("app", NAME))
            .endMetadata()
            .withNewSpec()
            .addToPorts(new ServicePortBuilder().withNewName("5432-tcp").withNewTargetPort(5432).withNewProtocol("TCP").withPort(5432).build())
            .addToSelector(TestUtils.map("app", NAME))
            .endSpec().done();

        // create a needed secret with the password
        OpenShiftUtils.getInstance().secrets().createOrReplaceWithNew()
            .withNewMetadata().withName("syndesis-global-config").endMetadata()
            .withStringData(TestUtils.map("POSTGRESQL_PASSWORD", "testpassword"))
            .withNewType("Opaque")
            .done();
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().services().withName(NAME).cascading(true).delete();
        OpenShiftUtils.getInstance().deploymentConfigs().withName(NAME).cascading(true).delete();
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("app", NAME));
    }
}
