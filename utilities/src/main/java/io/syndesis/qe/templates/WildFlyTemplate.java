package io.syndesis.qe.templates;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import io.fabric8.openshift.api.model.Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
public class WildFlyTemplate {

    public static void deploy(String gitURL, String appName) {
        if (!TestUtils.isDcDeployed(appName)) {
            Template template;
            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-wildfly.yml")) {
                template = OpenShiftUtils.client().templates().load(is).get();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read template ", ex);
            }

            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("GITHUB_REPO", gitURL);
            templateParams.put("APPLICATION_NAME", appName);

            OpenShiftUtils.client().templates().withName(appName).delete();

            OpenShiftUtils.client().imageStreams().createOrReplaceWithNew()
                    .editOrNewMetadata()
                    .withName("wildfly-130-centos7")
                    .addToLabels("app", appName)
                    .endMetadata()
                    .editOrNewSpec()
                    .editOrNewLookupPolicy()
                    .withLocal(false)
                    .endLookupPolicy()
                    .addNewTag()
                    .addToAnnotations("openshift.io/imported-from", "openshift/wildfly-130-centos7")
                    .editOrNewFrom()
                    .withKind("DockerImage")
                    .withName("openshift/wildfly-130-centos7")
                    .endFrom()
                    .withName("latest")
                    .editOrNewReferencePolicy()
                    .withType("Source")
                    .endReferencePolicy()
                    .endTag()
                    .endSpec()
                    .done();

            KubernetesList processedTemplate = OpenShiftUtils.getInstance().recreateAndProcessTemplate(template, templateParams);


            OpenShiftUtils.getInstance().createResources(processedTemplate);

            try {
                log.info("Waiting for " + appName + " to be ready");
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsRunning("deploymentconfig", appName, 1));
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for " + appName + " failed ", e);
            }

        }
    }

    public static void cleanUp(String appName){
        OpenShiftUtils.getInstance().getDeploymentConfigs().stream().filter(dc -> dc.getMetadata().getName().equals(appName)).findFirst()
                .ifPresent(dc -> OpenShiftUtils.getInstance().deleteDeploymentConfig(dc, true));
        OpenShiftUtils.getInstance().getServices().stream().filter(service -> appName.equals(service.getMetadata().getName())).findFirst()
                .ifPresent(service -> OpenShiftUtils.getInstance().deleteService(service));
        OpenShiftUtils.getInstance().getRoutes().stream().filter(route -> appName.equals(route.getMetadata().getName())).findFirst()
                .ifPresent(route -> OpenShiftUtils.getInstance().deleteRoute(route));
        OpenShiftUtils.getInstance().getImageStreams().stream().filter(is -> appName.equals(is.getMetadata().getName())).findFirst()
                .ifPresent(is -> OpenShiftUtils.getInstance().deleteImageStream(is));
        OpenShiftUtils.getInstance().getImageStreams().stream().filter(is -> "wildfly-130-centos7".equals(is.getMetadata().getName())).findFirst()
                .ifPresent(is -> OpenShiftUtils.getInstance().deleteImageStream(is));
        OpenShiftUtils.getInstance().getBuildConfigs().stream().filter(build -> appName.equals(build.getMetadata().getName())).findFirst()
                .ifPresent(build -> OpenShiftUtils.getInstance().deleteBuildConfig(build));
    }

}
