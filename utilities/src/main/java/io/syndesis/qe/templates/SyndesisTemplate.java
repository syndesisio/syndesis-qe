package io.syndesis.qe.templates;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.TodoUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
    private static final int IMAGE_STREAM_COUNT = 8;
    private static final String CR_NAME = "app";

    public static void deploy() {
        log.info("Deploying Syndesis");
        log.info("  Cluster:   " + TestConfiguration.openShiftUrl());
        log.info("  Namespace: " + TestConfiguration.openShiftNamespace());
        createPullSecret();
        deployUsingOperator();
        checkRoute();
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

    private static void deployUsingOperator() {
        log.info("Deploying using Operator");
        if (!TestUtils.isUserAdmin()) {
            StringBuilder sb = new StringBuilder("\n");
            sb.append("****************************************************\n");
            sb.append("* Operator deployment needs user with admin rights *\n");
            sb.append("****************************************************\n");
            sb.append(
                "If you are using minishift, you can use \"oc adm policy --as system:admin add-cluster-role-to-user cluster-admin developer\"\n");
            log.error(sb.toString());
            throw new RuntimeException(sb.toString());
        }

        deployCrd();
        deployOperator();
        deploySyndesisViaOperator();
        TodoUtils.createDefaultRouteForTodo("todo2", "/");
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

    private static void deployOperator() {
        final String operatorResourcesName = "syndesis-operator";
        String operatorImage = TestConfiguration.syndesisOperatorImage();
        String imageName = StringUtils.substringBeforeLast(operatorImage, ":");
        String imageTag = StringUtils.substringAfterLast(operatorImage, ":");

        log.info("Pulling operator image {}", operatorImage);
        ProcessBuilder dockerPullPb = new ProcessBuilder("docker",
            "pull",
            operatorImage
        );

        try {
            dockerPullPb.start().waitFor();
        } catch (Exception e) {
            log.error("Could not pull operator image", e);
            fail("Failed to pull operator");
        }

        log.info("Generating resources using operator image {}", operatorImage);
        ProcessBuilder dockerRunPb = new ProcessBuilder("docker",
            "run",
            "--rm",
            "--entrypoint",
            "syndesis-operator",
            operatorImage,
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
            resourceList = OpenShiftUtils.getInstance().load(p.getInputStream()).get();
            p.waitFor();
        } catch (Exception e) {
            log.error("Could not load resources from operator image", e);
            fail("Failed to install using operator");
        }

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
        if (TestConfiguration.syndesisUrl() != null) {
            envVarsToAdd.add(new EnvVar(
                "ROUTE_HOSTNAME",
                StringUtils.substringAfter(TestConfiguration.syndesisUrl(), "https://"),
                null)
            );
        } else {
            envVarsToAdd.add(new EnvVar(
                "ROUTE_HOSTNAME",
                TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix(),
                null)
            );
        }

        dc.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().addAll(envVarsToAdd);

        OpenShiftUtils.getInstance().createResources(resourceList);

        importProdImage("operator");

        log.info("Waiting for syndesis-operator to be ready");
        OpenShiftUtils.xtf().waiters()
            .areExactlyNPodsReady(1, "syndesis.io/component", "syndesis-operator")
            .interval(TimeUnit.SECONDS, 20)
            .timeout(TimeUnit.MINUTES, 10)
            .waitFor();
    }

    private static void deploySyndesisViaOperator() {
        log.info("Deploying syndesis resource from " + TestConfiguration.syndesisCrUrl());
        try (InputStream is = new URL(TestConfiguration.syndesisCrUrl()).openStream()) {
            Map<String, Object> cr = getSyndesisCrClient().load(is);

            Map<String, Object> spec = (Map<String, Object>) cr.get("spec");

            // setup integration limit and state check interval
            Map<String, Object> integration =
                (Map<String, Object>) spec.computeIfAbsent("integration", s -> new HashMap<String, Object>());
            if (TestUtils.isJenkins()) {
                integration.put("stateCheckInterval", TestConfiguration.stateCheckInterval());
            }

            // set correct image stream namespace
            spec.put("imageStreamNamespace", TestConfiguration.openShiftNamespace());

            // add nexus
            addMavenRepo(spec);

            getSyndesisCrClient()
                .create(TestConfiguration.openShiftNamespace(), cr);
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

    private static void addMavenRepo(Map<String, Object> spec) {
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

        Map<String, Object> mavenRepositories = (Map<String, Object>) spec
            .computeIfAbsent("mavenRepositories", s -> new HashMap<String, Object>());
        mavenRepositories.put("fuseqe_nexus", replacementRepo);
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
}
