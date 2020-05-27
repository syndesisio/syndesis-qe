package io.syndesis.qe.resource.impl;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.resource.Resource;
import io.syndesis.qe.utils.ODataUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.openshift.api.model.Template;
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
        if (!isDeployed()) {
            Template template;
            try (InputStream is = ClassLoader.getSystemResourceAsStream("templates/syndesis-wildfly.yml")) {
                template = OpenShiftUtils.getInstance().templates().load(is).get();
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read template ", ex);
            }

            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("GITHUB_REPO", gitURL);
            templateParams.put("APPLICATION_NAME", appName);
            templateParams.put("SOURCE_REF", branch == null ? "master" : branch);

            OpenShiftUtils.getInstance().templates().withName(appName).delete();

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

            OpenShiftUtils.getInstance().createResources(OpenShiftUtils.getInstance().recreateAndProcessTemplate(template,
                templateParams));
        }
    }

    @Override
    public void undeploy() {
        if (appName != null) {
            OpenShiftUtils.getInstance().routes().withName(appName).delete();
            OpenShiftUtils.getInstance().services().withName(appName).delete();
            OpenShiftUtils.getInstance().imageStreams().withName(appName).delete();
            OpenShiftUtils.getInstance().imageStreams().withName("wildfly-130-centos7").delete();
            OpenShiftUtils.getInstance().deploymentConfigs().withName(appName).cascading(true).delete();
            OpenShiftUtils.getInstance().buildConfigs().withName(appName).cascading(true).delete();
        }
    }

    @Override
    public boolean isReady() {
        return OpenShiftWaitUtils.isPodReady(OpenShiftUtils.getAnyPod("app", appName))
            && OpenShiftUtils.getPodLogs(appName).contains("OData service has started");
    }

    @Override
    public boolean isDeployed() {
        return TestUtils.isDcDeployed(appName);
    }

    public void createODataAccount(boolean https) {
        Account oData = new Account();
        oData.setService("OData");
        Map<String, String> properties = new HashMap<>();

        String serviceUri;
        String key;

        if (https) {
            serviceUri = "https://services.odata.org/TripPinRESTierService/";
            key = "odataHttps";
        } else {
            serviceUri = ODataUtils.getOpenshiftService();
            key = "odata";
        }

        properties.put("serviceUri", serviceUri);
        oData.setProperties(properties);
        AccountsDirectory.getInstance().addAccount(key, oData);
        log.info("Created new Account: {}", key);
    }
}
