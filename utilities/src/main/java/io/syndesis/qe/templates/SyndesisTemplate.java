package io.syndesis.qe.templates;

import cz.xtf.http.HttpClient;
import io.fabric8.openshift.api.model.DoneableTemplate;
import io.fabric8.openshift.client.dsl.TemplateResource;
import io.syndesis.qe.Component;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.openshift.api.model.Template;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyndesisTemplate {

    private static final String SUPPORT_SA_URL = TestConfiguration.syndesisTempalateSA();
    private static final String TEMPLATE_URL = TestConfiguration.syndesisTempalateUrl();

    public static Template getTemplate() {
        try (InputStream is = new URL(TEMPLATE_URL).openStream()) {
            return OpenShiftUtils.client().templates().load(is).get();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read template ", ex);
        }
    }

    public static ServiceAccount getSupportSA() {
        try (InputStream is = new URL(SUPPORT_SA_URL).openStream()) {
            return OpenShiftUtils.client().serviceAccounts().load(is).get();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read SA ", ex);
        }
    }

    public static void deploy() {
        OpenShiftUtils.getInstance().cleanAndAssert();

        // get & create restricted SA
        OpenShiftUtils.getInstance().createServiceAccount(getSupportSA());
        // get token from SA `oc secrets get-token` && wait until created to prevent 404
        TestUtils.waitForEvent(Optional::isPresent,
                () -> OpenShiftUtils.getInstance().getSecrets().stream().filter(s -> s.getMetadata().getName().startsWith("syndesis-oauth-client-token")).findFirst(),
                TimeUnit.MINUTES,
                2,
                TimeUnit.SECONDS,
                5);

        Secret secret = OpenShiftUtils.getInstance().getSecrets().stream().filter(s -> s.getMetadata().getName().startsWith("syndesis-oauth-client-token")).findFirst().get();
        // token is Base64 encoded by default
        String oauthTokenEncoded = secret.getData().get("token");
        byte[] oauthTokenBytes = Base64.decodeBase64(oauthTokenEncoded);
        String oauthToken = new String(oauthTokenBytes);

        // get the template
        Template template = getTemplate();
        // set params
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("ROUTE_HOSTNAME", TestConfiguration.openShiftNamespace() + "." + TestConfiguration.syndesisUrlSuffix());
        templateParams.put("OPENSHIFT_MASTER", TestConfiguration.openShiftUrl());
        templateParams.put("OPENSHIFT_PROJECT", TestConfiguration.openShiftNamespace());
        templateParams.put("OPENSHIFT_OAUTH_CLIENT_SECRET", oauthToken);
        templateParams.put("TEST_SUPPORT_ENABLED", "true");
        // process & create
        KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);
        OpenShiftUtils.getInstance().createResources(processedTemplate);
        OpenShiftUtils.createRestRoute(TestConfiguration.openShiftNamespace(), TestConfiguration.syndesisUrlSuffix());

        //TODO: there's a bug in openshift-client, we need to initialize manually
        OpenShiftUtils.client().roleBindings().createOrReplaceWithNew()
                .withNewMetadata()
                    .withName("syndesis:editors")
                .endMetadata()
                .withNewRoleRef().withName("edit").endRoleRef()
                .addNewSubject().withKind("ServiceAccount").withName(Component.SERVER.getName()).withNamespace(TestConfiguration.openShiftNamespace()).endSubject()
                .addToUserNames(String.format("system:serviceaccount:%s:%s", TestConfiguration.openShiftNamespace(), Component.SERVER.getName()))
                .done();
    }
}
