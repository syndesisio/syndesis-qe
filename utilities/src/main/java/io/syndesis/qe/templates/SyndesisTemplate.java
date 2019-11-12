package io.syndesis.qe.templates;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.Addon;
import io.syndesis.qe.Image;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.TodoUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyndesisTemplate {
    private static final String CR_NAME = "app";
    private static final String OPERATOR_IMAGE = TestConfiguration.syndesisOperatorImage();

    public static void deploy() {
        log.info("Deploying Syndesis");
        log.info("  Cluster:   " + TestConfiguration.openShiftUrl());
        log.info("  Namespace: " + TestConfiguration.openShiftNamespace());
        createPullSecret();
        deployCrd();
        pullOperatorImage();
        grantPermissions();
        deployUsingOperator();
        checkRoute();
        TodoUtils.createDefaultRouteForTodo("todo2", "/");
    }

    private static void createPullSecret() {
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
     * Deploys the operator and the custom resource.
     */
    private static void deployUsingOperator() {
        log.info("Deploying using Operator");
        deployOperator();
        deploySyndesisViaOperator();
    }

    /**
     * Pulls the operator image via docker pull.
     */
    private static void pullOperatorImage() {
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
    private static void grantPermissions() {
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
    private static void checkRoute() {
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

    public static Map<String, Object> getDeployedCr() {
        return getSyndesisCrClient().get(TestConfiguration.openShiftNamespace(), CR_NAME);
    }

    public static Map<String, Object> editCr(Map<String, Object> cr) throws IOException {
        return SyndesisTemplate.getSyndesisCrClient().edit(TestConfiguration.openShiftNamespace(), CR_NAME, cr);
    }

    public static void deleteCr() {
        getSyndesisCrClient().delete(TestConfiguration.openShiftNamespace(), CR_NAME);
    }

    public static Set<String> getCrNames() {
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

    public static RawCustomResourceOperationsImpl getSyndesisCrClient() {
        return OpenShiftUtils.getInstance().customResource(makeSyndesisContext());
    }

    public static CustomResourceDefinition getCrd() {
        return OpenShiftUtils.getInstance().customResourceDefinitions().withName("syndesises.syndesis.io").get();
    }

    private static CustomResourceDefinitionContext makeSyndesisContext() {
        CustomResourceDefinition syndesisCrd = getCrd();
        CustomResourceDefinitionContext.Builder builder = new CustomResourceDefinitionContext.Builder()
            .withGroup(syndesisCrd.getSpec().getGroup())
            .withPlural(syndesisCrd.getSpec().getNames().getPlural())
            .withScope(syndesisCrd.getSpec().getScope())
            .withVersion(syndesisCrd.getSpec().getVersion());
        CustomResourceDefinitionContext context = builder.build();

        return context;
    }

    private static void deployCrd() {
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

    public static List<HasMetadata> getOperatorResources() {
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

    private static void deployOperator() {
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

        importProdImage("operator");

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

    private static void deploySyndesisViaOperator() {
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

    private static void importProdImage(String imageStreamPartialName) {
        if (TestUtils.isProdBuild()) {
            int responseCode = -1;
            int retries = 0;
            while (responseCode != 201 && retries < 3) {
                if (retries != 0) {
                    TestUtils.sleepIgnoreInterrupt(15000L);
                }
                ImageStream is = OpenShiftUtils.getInstance().imageStreams().list().getItems().stream()
                    .filter(imgStream -> imgStream.getMetadata().getName().contains(imageStreamPartialName)).findFirst().get();
                Map<String, String> metadata = new HashMap<>();
                metadata.put("name", is.getMetadata().getName());
                metadata.put("namespace", is.getMetadata().getNamespace());
                // Sometimes the resource versions do not match, therefore it is needed to refresh the value
                metadata.put("resourceVersion",
                    OpenShiftUtils.getInstance().imageStreams().withName(is.getMetadata().getName()).get().getMetadata().getResourceVersion());

                log.info("Importing image from imagestream " + is.getMetadata().getName());
                HTTPResponse r = OpenShiftUtils.invokeApi(
                    HttpUtils.Method.POST,
                    String.format("/apis/image.openshift.io/v1/namespaces/%s/imagestreamimports", TestConfiguration.openShiftNamespace()),
                    ImageStreamImport.getJson(
                        new ImageStreamImport(is.getApiVersion(), metadata, is.getSpec().getTags().get(0).getFrom().getName(),
                            is.getSpec().getTags().get(0).getName())
                    )
                );
                responseCode = r.getCode();
                if (responseCode != 201 && retries == 2) {
                    fail("Unable to import image for image stream " + is.getMetadata().getName() + " after 3 retries");
                }

                retries++;
            }
        }
    }

    private static void addMavenRepo(JSONObject serverFeatures) {
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
     * Returns \"key\":\"value\".
     *
     * @param key key to use
     * @param value value to use
     * @return json
     */
    private static String jsonKeyValue(String key, String value) {
        if ("true".equals(value)) {
            // Don't quote the boolean value
            return "\"" + key + "\":" + value;
        } else {
            return "\"" + key + "\":\"" + value + "\"";
        }
    }

    @Data
    private static class ImageStreamImport {
        private String kind = "ImageStreamImport";
        private String apiVersion;
        private Map<String, String> metadata;
        @JsonIgnore
        private String image;
        @JsonIgnore
        private String name;

        ImageStreamImport(String apiVersion, Map metadata, String image, String name) {
            this.apiVersion = apiVersion;
            this.metadata = metadata;
            this.image = image;
            this.name = name;
        }

        public static String getJson(ImageStreamImport imageStreamImport) {
            ObjectMapper om = new ObjectMapper();
            om.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
            try {
                StringBuilder json = new StringBuilder("");
                StringBuilder spec = new StringBuilder("");
                String obj = om.writeValueAsString(imageStreamImport);
                json.append(obj.substring(0, obj.length() - 1));
                // Jackson can't serialize Map that is in the List that is in Map's Object value properly, therefore creating the json snippet
                // manually here
                spec.append("\"spec\":{").append(jsonKeyValue("import", "true")).append(",")
                    .append("\"images\":[{\"from\":{").append(jsonKeyValue("kind", "DockerImage")).append(",")
                    .append(jsonKeyValue("name", imageStreamImport.getImage()))
                    .append("},").append("\"to\":{").append(jsonKeyValue("name", imageStreamImport.getName())).append("},\"importPolicy\":{")
                    .append(jsonKeyValue("insecure", "true")).append("},\"referencePolicy\":{")
                    .append(jsonKeyValue("type", "")).append("}}]},\"status\":{}}");
                json.append(",").append(spec.toString());
                return json.toString();
            } catch (JsonProcessingException e) {
                fail("Unable to process json", e);
            }
            return null;
        }
    }

    /**
     * Checks if the given addon is enabled in the CR.
     *
     * @param addon addon to check
     * @return true/false
     */
    public static boolean isAddonEnabled(Addon addon) {
        try {
            JSONObject spec = new JSONObject(getSyndesisCrClient().get(TestConfiguration.openShiftNamespace(), CR_NAME))
                .getJSONObject("spec");

            // Special case for external DB
            if (addon == Addon.EXTERNAL_DB) {
                return spec.getJSONObject("components").getJSONObject(addon.getValue()).has("externalDbURL");
            } else {
                return Boolean.parseBoolean(spec.getJSONObject("addons").getJSONObject(addon.getValue()).getString("enabled"));
            }
        } catch (KubernetesClientException kce) {
            if (!kce.getMessage().contains("\\\"" + CR_NAME + "\\\" not found")) {
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
