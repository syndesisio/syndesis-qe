package io.syndesis.qe.resource.impl;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.Addon;
import io.syndesis.qe.Component;
import io.syndesis.qe.Image;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.TodoUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.openshift.api.model.DeploymentConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Syndesis implements Resource {
    private static final String CR_NAME = "app";
    private static final String OPERATOR_IMAGE = TestConfiguration.syndesisOperatorImage();

    @Override
    public void deploy() {
        log.info("Deploying Syndesis");
        log.info("  Cluster:   " + TestConfiguration.openShiftUrl());
        log.info("  Namespace: " + TestConfiguration.openShiftNamespace());
        createPullSecret();
        deployCrd();
        pullOperatorImage();
        grantPermissions();
        deployOperator();
        deploySyndesisViaOperator();
        checkRoute();
        TodoUtils.createDefaultRouteForTodo("todo2", "/");
    }

    @Override
    public void undeploy() {
        // Intentionally left blank to preserve current behavior - after test execution, syndesis was left installed and every other resource was
        // undeployed
        // We may need to revisit this later
        log.warn("Skipping Syndesis undeployment");
    }

    @Override
    public boolean isReady() {
        EnumSet<Component> components = Component.getAllComponents();
        List<Pod> syndesisPods = Component.getComponentPods();
        return syndesisPods.size() == components.size() && syndesisPods.stream().allMatch(OpenShiftWaitUtils::isPodReady);
    }

    public boolean isUndeployed() {
        List<Pod> syndesisPods = Component.getComponentPods();
        // Either 0 pods when the namespace was empty before undeploying, or 1 pod (the operator)
        return syndesisPods.size() == 0 || (syndesisPods.size() == 1 && syndesisPods.get(0).getMetadata().getName().startsWith("syndesis-operator"));
    }

    public void undeployCustomResources() {
        // if we don't have CRD, we can't have CRs
        if (getCrd() != null) {
            for (String s : getCrNames()) {
                undeployCustomResource(s);
            }
        }
    }

    /**
     * Undeploys syndesis custom resource using openshift API.
     *
     * @param name custom resource name
     */
    private void undeployCustomResource(String name) {
        deleteCr(name);
    }

    private void createPullSecret() {
        if (TestConfiguration.syndesisPullSecret() != null) {
            log.info("Creating a pull secret with name " + TestConfiguration.syndesisPullSecretName());
            OpenShiftUtils.getInstance().secrets().createOrReplaceWithNew()
                .withNewMetadata()
                .withName(TestConfiguration.syndesisPullSecretName())
                .endMetadata()
                .withData(TestUtils.map(".dockerconfigjson", TestConfiguration.syndesisPullSecret()))
                .withType("kubernetes.io/dockerconfigjson")
                .done();
        }
    }

    /**
     * Pulls the operator image via docker pull.
     */
    public void pullOperatorImage() {
        log.info("Pulling operator image {}", OPERATOR_IMAGE);
        ProcessBuilder dockerPullPb = new ProcessBuilder("docker",
            "pull",
            OPERATOR_IMAGE
        );

        try {
            dockerPullPb.start().waitFor();
        } catch (Exception e) {
            log.error("Could not pull operator image", e);
            fail("Failed to pull operator");
        }
    }

    /**
     * Grants the permissions via the admin user to the regular user.
     */
    public void grantPermissions() {
        log.info("Granting permissions to user {}", TestConfiguration.syndesisUsername());
        new File(OpenShiftUtils.binary().getOcConfigPath()).setReadable(true, false);

        try {
            new ProcessBuilder("docker",
                "run",
                "--rm",
                "-v",
                OpenShiftUtils.binary().getOcConfigPath() + ":/tmp/kube/config:z",
                "--entrypoint",
                "syndesis-operator",
                TestConfiguration.syndesisOperatorImage(),
                "grant",
                "-u",
                TestConfiguration.syndesisUsername(),
                "--namespace",
                TestConfiguration.openShiftNamespace(),
                "--config",
                "/tmp/kube/config"
            ).start().waitFor();
        } catch (Exception e) {
            log.error("Unable to grant permissions", e);
            fail("Unable to grant permissions from docker run");
        }
    }

    /**
     * In case of multiple uses of a static route, openshift will create the route anyway with a false condition, so rather fail fast.
     */
    private void checkRoute() {
        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().routes().withName("syndesis").get() != null, 120000L);
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().routes().withName("syndesis").get()
                .getStatus().getIngress() != null, 120000L);
        } catch (Exception e) {
            fail("Unable to find syndesis route in 120s");
        }

        if ("false".equalsIgnoreCase(
            OpenShiftUtils.getInstance().routes().withName("syndesis").get().getStatus().getIngress().get(0).getConditions().get(0).getStatus())) {
            fail("Syndesis route failed to provision because of: " +
                OpenShiftUtils.getInstance().routes().withName("syndesis").get().getStatus().getIngress().get(0).getConditions().get(0).getMessage());
        }
    }

    public Map<String, Object> getDeployedCr() {
        return getSyndesisCrClient().get(TestConfiguration.openShiftNamespace(), CR_NAME);
    }

    public Map<String, Object> editCr(Map<String, Object> cr) throws IOException {
        return getSyndesisCrClient().edit(TestConfiguration.openShiftNamespace(), CR_NAME, cr);
    }

    private void deleteCr(String name) {
        getSyndesisCrClient().delete(TestConfiguration.openShiftNamespace(), name);
    }

    private Set<String> getCrNames() {
        final Set<String> names = new HashSet<>();
        Map<String, Object> crs = getSyndesisCrClient().list(TestConfiguration.openShiftNamespace());
        JSONArray items = new JSONArray();
        try {
            items = new JSONObject(crs).getJSONArray("items");
        } catch (JSONException ex) {
            // probably the CRD isn't present in the cluster
        }
        for (int i = 0; i < items.length(); i++) {
            names.add(items.getJSONObject(i).getJSONObject("metadata").getString("name"));
        }

        return names;
    }

    public RawCustomResourceOperationsImpl getSyndesisCrClient() {
        return OpenShiftUtils.getInstance().customResource(makeSyndesisContext());
    }

    public CustomResourceDefinition getCrd() {
        return OpenShiftUtils.getInstance().customResourceDefinitions().withName("syndesises.syndesis.io").get();
    }

    private CustomResourceDefinitionContext makeSyndesisContext() {
        CustomResourceDefinition syndesisCrd = getCrd();
        CustomResourceDefinitionContext.Builder builder = new CustomResourceDefinitionContext.Builder()
            .withGroup(syndesisCrd.getSpec().getGroup())
            .withPlural(syndesisCrd.getSpec().getNames().getPlural())
            .withScope(syndesisCrd.getSpec().getScope())
            .withVersion(syndesisCrd.getSpec().getVersion());
        return builder.build();
    }

    public void deployCrd() {
        log.info("Creating custom resource definition from " + TestConfiguration.syndesisCrdUrl());
        try (InputStream is = new URL(TestConfiguration.syndesisCrdUrl()).openStream()) {
            CustomResourceDefinition crd = OpenShiftUtils.getInstance().customResourceDefinitions().load(is).get();
            OpenShiftUtils.getInstance().customResourceDefinitions().create(crd);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load CRD", ex);
        } catch (KubernetesClientException kce) {
            if (!kce.getMessage().contains("already exists")) {
                throw kce;
            }
        }
    }

    public List<HasMetadata> getOperatorResources() {
        String imageName = StringUtils.substringBeforeLast(OPERATOR_IMAGE, ":");
        String imageTag = StringUtils.substringAfterLast(OPERATOR_IMAGE, ":");

        log.info("Generating resources using operator image {}", OPERATOR_IMAGE);
        ProcessBuilder dockerRunPb = new ProcessBuilder("docker",
            "run",
            "--rm",
            "--entrypoint",
            "syndesis-operator",
            OPERATOR_IMAGE,
            "install",
            "operator",
            "--image",
            imageName,
            "--tag",
            imageTag,
            "-e", "yaml"
        );

        List<HasMetadata> resourceList = null;
        try {
            Process p = dockerRunPb.start();
            final String resources = IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
            log.debug("Resources generated from the operator image");
            log.debug(resources);
            resourceList = OpenShiftUtils.getInstance().load(IOUtils.toInputStream(resources, StandardCharsets.UTF_8)).get();
            p.waitFor();
        } catch (Exception e) {
            log.error("Could not load resources from operator image, check debug logs", e);
            fail("Failed to install using operator");
        }

        return resourceList;
    }

    public void deployOperator() {
        List<HasMetadata> resourceList = getOperatorResources();
        final String operatorResourcesName = "syndesis-operator";
        Optional<HasMetadata> serviceAccount = resourceList.stream()
            .filter(resource -> "ServiceAccount".equals(resource.getKind()) && operatorResourcesName.equals(resource.getMetadata().getName()))
            .findFirst();

        if (serviceAccount.isPresent()) {
            ((ServiceAccount) serviceAccount.get())
                .getImagePullSecrets().add(new LocalObjectReference(TestConfiguration.syndesisPullSecretName()));
        } else {
            log.error("Service account not found in resources");
        }

        DeploymentConfig dc = (DeploymentConfig) resourceList.stream()
            .filter(r -> "DeploymentConfig".equals(r.getKind()) && operatorResourcesName.equals(r.getMetadata().getName()))
            .findFirst().orElseThrow(() -> new RuntimeException("Unable to find deployment config in operator resources"));

        List<EnvVar> envVarsToAdd = new ArrayList<>();
        envVarsToAdd.add(new EnvVar("TEST_SUPPORT", "true", null));

        dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().addAll(envVarsToAdd);

        List<HasMetadata> finalResourceList = resourceList;
        OpenShiftUtils.asRegularUser(() -> OpenShiftUtils.getInstance().createResources(finalResourceList));

        Set<Image> images = EnumSet.allOf(Image.class);
        Map<String, String> imagesEnvVars = new HashMap<>();
        for (Image image : images) {
            if (TestConfiguration.image(image) != null) {
                log.info("Will override " + image.name().toLowerCase() + " image with " + TestConfiguration.image(image));
                imagesEnvVars.put(image.name() + "_IMAGE", TestConfiguration.image(image));
            }
        }

        if (!imagesEnvVars.isEmpty()) {
            log.info("Overriding images to be deployed");
            try {
                OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getInstance().getDeploymentConfig(operatorResourcesName) != null);
                OpenShiftUtils.getInstance().scale(operatorResourcesName, 0);
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areNoPodsPresent(operatorResourcesName));
            } catch (Exception e) {
                e.printStackTrace();
            }

            OpenShiftUtils.getInstance().updateDeploymentConfigEnvVars(operatorResourcesName, imagesEnvVars);
            try {
                OpenShiftUtils.getInstance().scale(operatorResourcesName, 1);
            } catch (KubernetesClientException ex) {
                // retry one more time after a slight delay
                log.warn("Caught KubernetesClientException: " + ex);
                log.warn("Will retry in 30 seconds");
                TestUtils.sleepIgnoreInterrupt(30000L);
                OpenShiftUtils.getInstance().scale(operatorResourcesName, 1);
            }
        }

        log.info("Waiting for syndesis-operator to be ready");
        OpenShiftUtils.getInstance().waiters()
            .areExactlyNPodsReady(1, "syndesis.io/component", operatorResourcesName)
            .interval(TimeUnit.SECONDS, 20)
            .timeout(TimeUnit.MINUTES, 10)
            .waitFor();
    }

    private void deploySyndesisViaOperator() {
        log.info("Deploying syndesis resource from " + TestConfiguration.syndesisCrUrl());
        try (InputStream is = new URL(TestConfiguration.syndesisCrUrl()).openStream()) {
            JSONObject crJson = new JSONObject(getSyndesisCrClient().load(is));

            JSONObject serverFeatures = crJson.getJSONObject("spec").getJSONObject("components")
                .getJSONObject("server").getJSONObject("features");
            if (TestUtils.isJenkins()) {
                serverFeatures.put("integrationStateCheckInterval", TestConfiguration.stateCheckInterval());
            }
            serverFeatures.put("integrationLimit", 5);

            // set correct image stream namespace
            crJson.getJSONObject("spec").put("imageStreamNamespace", TestConfiguration.openShiftNamespace());

            // set the route
            crJson.getJSONObject("spec").put("routeHostname", TestConfiguration.syndesisUrl() != null
                ? StringUtils.substringAfter(TestConfiguration.syndesisUrl(), "https://")
                : TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix());

            // add nexus
            addMavenRepo(serverFeatures);

            getSyndesisCrClient().create(TestConfiguration.openShiftNamespace(), crJson.toMap());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load operator syndesis template", ex);
        }
    }

    private void addMavenRepo(JSONObject serverFeatures) {
        String replacementRepo = null;
        if (TestUtils.isProdBuild()) {
            if (TestConfiguration.prodRepository() != null) {
                replacementRepo = TestConfiguration.prodRepository();
            } else {
                fail("Trying to deploy prod version using operator and system property " + TestConfiguration.PROD_REPOSITORY + " is not set!");
            }
        } else {
            if (TestConfiguration.upstreamRepository() != null) {
                replacementRepo = TestConfiguration.upstreamRepository();
            } else {
                // no replacement, will use maven central
                log.warn("No repo to add, skipping");
                return;
            }
        }
        log.info("Adding maven repo {}", replacementRepo);

        serverFeatures.put("mavenRepositories", TestUtils.map("fuseqe_nexus", replacementRepo));
    }

    /**
     * Checks if the given addon is enabled in the CR.
     *
     * @param addon addon to check
     * @return true/false
     */
    public boolean isAddonEnabled(Addon addon) {
        try {
            JSONObject spec = new JSONObject(getSyndesisCrClient().get(TestConfiguration.openShiftNamespace(), CR_NAME))
                .getJSONObject("spec");

            // Special case for external DB
            if (addon == Addon.EXTERNAL_DB) {
                return spec.getJSONObject("components").getJSONObject(addon.getValue()).has("externalDbURL");
            } else {
                return spec.getJSONObject("addons").getJSONObject(addon.getValue()).getBoolean("enabled");
            }
        } catch (KubernetesClientException kce) {
            if (!kce.getMessage().contains("404")) {
                // If the error is something different than the CR wasn't found rethrow the exception
                throw kce;
            }
            return false;
        } catch (JSONException e) {
            // ignore exception as some of the object wasn't present
            return false;
        }
    }
}
