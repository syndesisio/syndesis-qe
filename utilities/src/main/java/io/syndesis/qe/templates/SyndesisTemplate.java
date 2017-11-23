package io.syndesis.qe.templates;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.openshift.api.model.Template;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyndesisTemplate {

	private static final String SUPPORT_SA_URL = "https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/support/serviceaccount-as-oauthclient-restricted.yml";
	private static final String TEMPLATE_URL = "https://raw.githubusercontent.com/syndesisio/syndesis-openshift-templates/master/syndesis-restricted.yml";

	public static Template getTemplate() {
		try (InputStream is = new URL(TEMPLATE_URL).openStream()) {
			return OpenShiftUtils.getInstance().withDefaultUser(client -> client.templates().load(is).get());
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to read template ", ex);
		}
	}

	public static ServiceAccount getSupportSA() {
		try (InputStream is = new URL(SUPPORT_SA_URL).openStream()) {
			return OpenShiftUtils.getInstance().withDefaultUser(client -> client.serviceAccounts().load(is).get());
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to read SA ", ex);
		}
	}

	public static void deploy() {
		OpenShiftUtils.getInstance().cleanProject();

		// get & create restricted SA
		OpenShiftUtils.getInstance().withDefaultUser(client -> client.serviceAccounts().createOrReplace(getSupportSA()));
		OpenShiftUtils.getInstance().getServiceAccounts().stream().forEach(sa -> log.info(sa.getMetadata().getName()));
		// get token from SA `oc secrets get-token`
		Secret secret = OpenShiftUtils.getInstance().getSecrets().stream().filter(s -> s.getMetadata().getName().startsWith("syndesis-oauth-client-token")).findFirst().get();
		String oauthToken = secret.getData().get("token");

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
		KubernetesList processedTemplate = OpenShiftUtils.getInstance().processTemplate(template, templateParams);
		OpenShiftUtils.getInstance().createResources(processedTemplate);
		OpenShiftUtils.getInstance().createRestRoute();
		try {
			OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("component", "syndesis-rest"));
		} catch (InterruptedException | TimeoutException e) {
			log.error("Wait for syndesis-rest failed ", e);
		}
	}
}
