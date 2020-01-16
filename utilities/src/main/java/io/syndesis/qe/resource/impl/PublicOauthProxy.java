package io.syndesis.qe.resource.impl;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublicOauthProxy implements Resource {
    private static final String TEMPLATE_NAME = "syndesis-public-oauthproxy";
    public static final String PUBLIC_API_PROXY_ROUTE = "public-" + TestConfiguration.openShiftNamespace()
        + "." + TestConfiguration.openShiftRouteSuffix();

    @Override
    public void deploy() {
        if (OpenShiftUtils.getInstance().getTemplate(TEMPLATE_NAME) != null ||
            OpenShiftWaitUtils.isAPodReady("syndesis.io/component", TEMPLATE_NAME).getAsBoolean()) {
            undeploy();
        }
        log.info("Creating {} template", TEMPLATE_NAME);

        OpenShiftUtils.binary().execute(
            "apply",
            "-f", TestConfiguration.syndesisPublicOauthProxyTemplateUrl()
        );
        clearClusterRoleBindings();
        OpenShiftUtils.binary().execute(
            "new-app",
            "--template", "syndesis-public-oauthproxy",
            "-p", "PUBLIC_API_ROUTE_HOSTNAME=" + PUBLIC_API_PROXY_ROUTE,
            "-p", "OPENSHIFT_PROJECT=" + TestConfiguration.openShiftNamespace(),
            "-p", "OPENSHIFT_OAUTH_CLIENT_SECRET=" + getOathToken(),
            "-p", "OAUTH_PROXY_TAG=" + OpenShiftUtils.getInstance().getImageStream("oauth-proxy").getSpec().getTags().get(0).getName(),
            "-p", "SAR_PROJECT=" + TestConfiguration.openShiftSARNamespace()
        );
    }

    @Override
    public void undeploy() {
        log.info("Cleaning up everything for {} template", TEMPLATE_NAME);
        OpenShiftUtils.getInstance().deploymentConfigs().withName(TEMPLATE_NAME).delete();
        OpenShiftUtils.getInstance().services().withName(TEMPLATE_NAME).delete();
        OpenShiftUtils.getInstance().routes().withName("syndesis-public-api").delete();
        OpenShiftUtils.getInstance().serviceAccounts().withName(TEMPLATE_NAME).delete();
        OpenShiftUtils.getInstance().roleBindings().withName("syndesis-public-oauthproxy:viewers").delete();
        // OpenShiftUtils.getInstance() doesn't provide clusterrolebindings
        clearClusterRoleBindings();
        OpenShiftUtils.getInstance().deleteTemplate(TEMPLATE_NAME);
        OpenShiftUtils.getInstance().getPods().stream().filter(pod -> pod.getMetadata().getName().contains("syndesis-public-oauthproxy"))
            .findFirst()
            .ifPresent(pod -> OpenShiftUtils.getInstance().deletePod(pod));
        TestUtils.sleepIgnoreInterrupt(10 * 1000);
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("syndesis.io/component", TEMPLATE_NAME));
    }

    private static String getOathToken() {
        return OpenShiftUtils.binary().execute("sa", "get-token", "syndesis-oauth-client");
    }

    private static void clearClusterRoleBindings() {
        OpenShiftUtils.binary().execute(
            "delete",
            "clusterrolebindings.authorization.openshift.io",
            "syndesis-" + TestConfiguration.openShiftNamespace() + "-auth-delegator"
        );
    }
}
