package io.syndesis.qe.templates;

import cz.xtf.openshift.OpenShiftBinaryClient;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

@Slf4j
public class PublicOauthProxyTemplate {

    private static final String TEMPLATE_NAME = "syndesis-public-oauthproxy";
    public static final String PUBLIC_API_PROXY_ROUTE = "public-" + TestConfiguration.openShiftNamespace() + "." + TestConfiguration.openShiftRouteSuffix();

    public static void deploy() {
        if (OpenShiftUtils.getInstance().getTemplate(TEMPLATE_NAME) == null || !OpenShiftWaitUtils.isAPodReady("syndesis.io/component", TEMPLATE_NAME).getAsBoolean()) {
            OpenShiftBinaryClient.getInstance().project(TestConfiguration.openShiftNamespace());

            log.info("Creating {} template", TEMPLATE_NAME);

            OpenShiftBinaryClient.getInstance().executeCommand(
                    "Unable to create syndesis public oauthproxy template " + TestConfiguration.syndesisPublicOauthProxyTemplateUrl(),
                    "apply",
                    "-f", TestConfiguration.syndesisPublicOauthProxyTemplateUrl()
            );
            clearClusterRoleBindings();
            OpenShiftBinaryClient.getInstance().executeCommand(
                    "Unable to process syndesis public oauthproxy template " + TestConfiguration.syndesisPublicOauthProxyTemplateUrl(),
                    "new-app",
                    "--template", "syndesis-public-oauthproxy",
                    "-p", "PUBLIC_API_ROUTE_HOSTNAME=" + PUBLIC_API_PROXY_ROUTE,
                    "-p", "OPENSHIFT_PROJECT=" + TestConfiguration.openShiftNamespace(),
                    "-p", "OPENSHIFT_OAUTH_CLIENT_SECRET=" + getOathToken(),
                    "-p", "SAR_PROJECT=" + TestConfiguration.openShiftSARNamespace()
            );
            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("syndesis.io/component", TEMPLATE_NAME));
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for {} template failed ", TEMPLATE_NAME, e);
            }
        }
    }

    public static void cleanUp() {
        log.info("Cleaning up everything for {} template", TEMPLATE_NAME);
        OpenShiftBinaryClient.getInstance().project(TestConfiguration.openShiftNamespace());

        OpenShiftUtils.getInstance().getServiceAccounts().stream().filter(sa -> "syndesis-public-oauthproxy".equals(sa.getMetadata().getName())).findFirst()
                .ifPresent(sa -> OpenShiftUtils.getInstance().deleteServiceAccount(sa));
        OpenShiftUtils.getInstance().getRoleBindings().stream().filter(rb -> "syndesis-public-oauthproxy:viewers".equals(rb.getMetadata().getName())).findFirst()
                .ifPresent(rb -> OpenShiftUtils.getInstance().deleteRoleBinding(rb));
        // OpenShiftUtils.getInstance() doesn't provide clusterrolebindings
        clearClusterRoleBindings();
        OpenShiftUtils.getInstance().getImageStreams().stream().filter(is -> "syndesis-public-oauthproxy".equals(is.getMetadata().getName())).findFirst()
                .ifPresent(is -> OpenShiftUtils.getInstance().deleteImageStream(is));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> "syndesis-public-oauthproxy".equals(service.getMetadata().getName())).findFirst()
                .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> "syndesis-public-api".equals(route.getMetadata().getName())).findFirst()
                .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> "syndesis-public-oauthproxy".equals(dc.getMetadata().getName())).findFirst()
                .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));

        TestUtils.sleepIgnoreInterrupt(10 * 1000);
    }

    private static String getOathToken() {
        return OpenShiftBinaryClient.getInstance().executeCommandWithReturn("", "sa", "get-token", "syndesis-oauth-client");
    }

    private static void clearClusterRoleBindings() {
        OpenShiftBinaryClient.getInstance().executeCommandWithReturn(
                "Unable to delete cluster role bindings",
                "delete",
                "clusterrolebindings.authorization.openshift.io",
                "syndesis-syndesis-auth-delegator"
        );
    }
}
