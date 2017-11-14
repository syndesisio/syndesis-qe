package io.syndesis.qe.utils;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import io.fabric8.openshift.client.ParameterValue;
import io.syndesis.qe.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.observables.StringObservable;

/**
 * OpenShift utils.
 *
 * Sep 8, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class OpenShiftUtils {

	private static NamespacedOpenShiftClient client;

	private static OpenShiftUtils INSTANCE = null;

	public static OpenShiftUtils getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new OpenShiftUtils();
		}
		return INSTANCE;
	}

	private OpenShiftUtils() {
		//no op
	}

	public <R> R withDefaultUser(Function<NamespacedOpenShiftClient, R> f) {
		if (client == null) {

			final OpenShiftConfigBuilder openShiftConfigBuilder = new OpenShiftConfigBuilder()
					.withMasterUrl(TestConfiguration.openShiftUrl())
					.withTrustCerts(true)
					.withRequestTimeout(120_000)
					.withNamespace(TestConfiguration.openShiftNamespace());

			if (!TestConfiguration.openShiftToken().isEmpty()) {
				//if token is provided, lets use it
				//otherwise f8 client should be able to leverage ~/.kube/config or mounted secrets
				openShiftConfigBuilder.withOauthToken(TestConfiguration.openShiftToken());
			}

			client = new DefaultOpenShiftClient(openShiftConfigBuilder.build());
		}

		return f.apply(client);
	}

	public Pod createPod(Pod pod) {
		return withDefaultUser(client -> client.pods().create(pod));
	}

	public Collection<Pod> getPods() {
		return getPods(TestConfiguration.openShiftNamespace());
	}

	public Collection<Pod> getPods(String namespace) {
		log.debug("Getting pods for namespace {}", namespace);
		return withDefaultUser(client -> client.inNamespace(namespace).pods().list().getItems());
	}

	private List<Pod> findPods(Map<String, String> labels) {
		return withDefaultUser(client -> client.inNamespace(TestConfiguration.openShiftNamespace())
				.pods().withLabels(labels).list().getItems());
	}

	public List<Pod> findComponentPods(String name) {
		return findPods(Collections.singletonMap("component", name));
	}

	public Pod findComponentPod(String name) {
		Collection<Pod> pods = findComponentPods(name);
		if (pods.size() != 1) {
			throw new IllegalStateException("Expected one named pod but got "
					+ pods.size());
		}
		return pods.iterator().next();
	}

	public void deletePod(Pod pod) {
		withDefaultUser(client -> client.pods().delete(pod));
	}

	public Collection<DeploymentConfig> getDeployments() {
		return withDefaultUser(client -> client.deploymentConfigs().list()
				.getItems());
	}

	public void deleteDeploymentConfig(boolean cascading, DeploymentConfig deploymentConfig) {
		withDefaultUser(client -> client.deploymentConfigs().withName(deploymentConfig.getMetadata().getName()).cascading(cascading).delete());
	}

	public Collection<BuildConfig> getBuildConfigs() {
		return withDefaultUser(client -> client.buildConfigs().list()
				.getItems());
	}

	public void deleteBuildConfig(BuildConfig buildConfig) {
		withDefaultUser(client -> client.buildConfigs().delete(buildConfig));
	}

	public Collection<ReplicationController> getReplicationControllers() {
		return withDefaultUser(client -> client.replicationControllers().list()
				.getItems());
	}

	public void deleteReplicationController(boolean cascading, ReplicationController rc) {
		withDefaultUser(client -> client.replicationControllers().withName(rc.getMetadata().getName()).cascading(cascading).delete());
	}

	public Collection<ImageStream> getImageStreams() {
		return withDefaultUser(client -> client.imageStreams().list().getItems());
	}

	public boolean deleteImageStream(ImageStream imageStream) {
		return withDefaultUser(client -> client.imageStreams().delete(imageStream));
	}

	public Collection<Service> getServices() {
		return withDefaultUser(client -> client.services().list().getItems());
	}

	public void deleteService(Service service) {
		withDefaultUser(client -> client.services().delete(service));
	}

	public Collection<Build> getBuilds() {
		return withDefaultUser(client -> client.builds().list().getItems());
	}

	public void deleteBuild(Build build) {
		withDefaultUser(client -> client.builds().delete(build));
	}

	public Collection<Route> getRoutes() {
		return withDefaultUser(client -> client.routes().list().getItems());
	}

	public void deleteRoute(Route route) {
		withDefaultUser(client -> client.resource(route).delete());
	}

	public Route createRestRoute() {

		final Route route = new RouteBuilder()
					.withNewMetadata()
						.withName("syndesis-rest")
					.endMetadata()
					.withNewSpec()
						.withPath("/api").withHost("rest-" + TestConfiguration.openShiftNamespace() + "." + TestConfiguration.syndesisUrlSuffix())
						.withWildcardPolicy("None")
						.withNewTls()
							.withTermination("edge")
						.endTls()
						.withNewTo()
							.withKind("Service").withName("syndesis-rest")
						.endTo()
					.endSpec()
				.build();

		return withDefaultUser(client -> client.resource(route).createOrReplace());
	}

	public Collection<PersistentVolumeClaim> getPersistentVolumeClaims() {
		return withDefaultUser(client -> client.persistentVolumeClaims().list()
				.getItems());
	}

	public void deletePersistentVolumeClaim(PersistentVolumeClaim pvc) {
		withDefaultUser(client -> client.persistentVolumeClaims().delete(pvc));
	}

	public Collection<ConfigMap> getConfigMaps() {
		return withDefaultUser(client -> client.configMaps().list().getItems());
	}

	public void deleteConfigMap(final ConfigMap configMap) {
		withDefaultUser(client -> client.configMaps().delete(configMap));
	}

	public Collection<Secret> getSecrets() {
		return withDefaultUser(client -> client.secrets().list().getItems());
	}

	public void deleteSecret(Secret secret) {
		withDefaultUser(client -> client.secrets().delete(secret));
	}

	public Collection<ServiceAccount> getServiceAccounts() {
		return withDefaultUser(client -> client.serviceAccounts().list()
				.getItems());
	}

	public void deleteServiceAccount(ServiceAccount serviceAccount) {
		withDefaultUser(client -> client.serviceAccounts().delete(serviceAccount));
	}

	public void cleanProject() {
		cleanProject(2);
	}

	public Collection<RoleBinding> getRoleBindings() {
		return withDefaultUser(client -> client.roleBindings().list().getItems());
	}

	public void deleteRoleBinding(RoleBinding roleBinding) {
		withDefaultUser(client -> client.roleBindings().delete(roleBinding));
	}

	public KubernetesList processTemplate(Template template, Map<String, String> parameters) {
		ParameterValue[] values = processParameters(parameters);

		return withDefaultUser(client -> {
			client.templates().createOrReplace(template);

			return client.templates().withName(template.getMetadata().getName()).process(values);
		});
	}

	public LocalPortForward portForward(Pod pod, int remotePort, int localPort) {
		return withDefaultUserPod(pod).portForward(remotePort, localPort);
	}

	public String getRuntimeLog(final Pod pod) {
		return withDefaultUserPod(pod).getLog();
	}

	public Observable<String> observeRuntimeLog(final Pod pod) {
		final LogWatch watcher = withDefaultUserPod(pod).watchLog();
		return StringObservable.byLine(StringObservable
				.from(new InputStreamReader(watcher.getOutput())));
	}

	private PodResource<Pod, DoneablePod> withDefaultUserPod(Pod pod) {
		if (pod.getMetadata().getNamespace() != null) {
			return withDefaultUser(c -> c.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName()));
		} else {
			return withDefaultUser(c -> c.pods().withName(pod.getMetadata().getName()));
		}
	}

	private ParameterValue[] processParameters(Map<String, String> parameters) {
		return parameters
				.entrySet()
				.stream()
				.map(entry -> new ParameterValue(entry.getKey(), entry
						.getValue())).collect(Collectors.toList())
				.toArray(new ParameterValue[parameters.size()]);
	}

	public KubernetesList createResources(KubernetesList list) {
		return (KubernetesList) this.withDefaultUser((client) -> {
			return (KubernetesList) client.lists().create(new KubernetesList[]{list});
		});
	}

	private void cleanProject(int count) {
		if (TestConfiguration.openShiftNamespace().matches("\\s*|\\s*default\\s*")) {
			throw new IllegalStateException(
					"Attempt to clean default namespace!");
		}

		// keep the order for deletion to prevent K8s creating resources again
		getDeployments().forEach(x -> deleteDeploymentConfig(false, x));
		getBuildConfigs().forEach(this::deleteBuildConfig);
		getReplicationControllers().forEach(x -> deleteReplicationController(false, x));
		getImageStreams().forEach(this::deleteImageStream);
		getServices().forEach(this::deleteService);
		getBuilds().forEach(this::deleteBuild);
		getRoutes().forEach(this::deleteRoute);
		getPods().forEach(this::deletePod);
		getPersistentVolumeClaims().forEach(this::deletePersistentVolumeClaim);
		getConfigMaps().forEach(this::deleteConfigMap);
		// Remove only user secrets
		getSecrets().stream()
				.filter(s -> !s.getType().startsWith("kubernetes.io/"))
				.forEach(this::deleteSecret);

		// Remove only users service accounts
		getServiceAccounts().stream()
				.filter(sa -> !sa.getMetadata().getName().equals("builder"))
				.filter(sa -> !sa.getMetadata().getName().equals("default"))
				.filter(sa -> !sa.getMetadata().getName().equals("deployer"))
				.forEach(this::deleteServiceAccount);

		// Remove only users service accounts
		getRoleBindings().stream()
				.filter(rb -> rb.getMetadata().getName().startsWith("syndesis"))
				.forEach(this::deleteRoleBinding);

		if (count > 0) {
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException e) {
				log.warn("Interrupted while cleaning project.", e);
			}
			cleanProject(count - 1);
		}
	}
}
