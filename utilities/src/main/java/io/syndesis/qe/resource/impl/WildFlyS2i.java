package io.syndesis.qe.resource.impl;

import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class WildFlyS2i implements Resource {
    private String gitURL;
    private String appName;
    private String branch;

    @Override
    public void deploy() {
        if (!TestUtils.isDcDeployed(appName)) {
            //            Template template;
            //            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-wildfly.yml")) {
            //                template = OpenShiftUtils.getInstance().templates().load(is).get();
            //            } catch (IOException ex) {
            //                throw new IllegalArgumentException("Unable to read template ", ex);
            //            }
            //
            //            Map<String, String> templateParams = new HashMap<>();
            //            templateParams.put("GITHUB_REPO", gitURL);
            //            templateParams.put("APPLICATION_NAME", appName);
            //
            //            OpenShiftUtils.getInstance().templates().withName(appName).delete();

            OpenShiftUtils.getInstance().imageStreams().createOrReplaceWithNew()
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

            //OCP4HACK - openshift-client 4.3.0 isn't supported with OCP4 and can't create/delete templates, following line can be removed later
            OpenShiftUtils.binary()
                .execute("create", "-f", Paths.get("../utilities/src/main/resources/templates/syndesis-wildfly.yml").toAbsolutePath().toString());
            OpenShiftUtils.binary()
                .execute("new-app", "wildfly-s2i-template", "-p", "GITHUB_REPO=" + gitURL, "-p", "APPLICATION_NAME=" + appName, "-p",
                    "SOURCE_REF=" + (branch != null ? branch : "master"));

            //            OpenShiftUtils.getInstance().createResources(OpenShiftUtils.getInstance().recreateAndProcessTemplate(template,
            //            templateParams));

            try {
                log.info("Waiting for " + appName + " to be ready");
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsRunning("deploymentconfig", appName, 1));
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for " + appName + " failed ", e);
            }

            try {
                OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.getPodLogs(appName).contains("OData service has started"), 5 * 60 * 1000L);
            } catch (TimeoutException | InterruptedException e) {
                log.error(appName + " pod logs did not contain expected message. Pod logs:");
                log.error(OpenShiftUtils.getPodLogs(appName));
            }

        }
    }

    @Override
    public void undeploy() {
        OpenShiftUtils.getInstance().routes().withName(appName).delete();
        OpenShiftUtils.getInstance().services().withName(appName).delete();
        OpenShiftUtils.getInstance().imageStreams().withName(appName).delete();
        OpenShiftUtils.getInstance().imageStreams().withName("wildfly-130-centos7").delete();
        OpenShiftUtils.getInstance().deploymentConfigs().withName(appName).cascading(true).delete();
        OpenShiftUtils.getInstance().buildConfigs().withName(appName).cascading(true).delete();
    }
}
