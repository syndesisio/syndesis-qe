package io.syndesis.qe.util;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;

public class IntegrationPodUtil {
    public static Document getIntegrationXml(String integrationName) {
        final String integrationPodName = OpenShiftUtils.getPodByPartialName("i-" + integrationName).get().getMetadata().getName();
        // It adds quotes around the command for exec and oc client doesn't understand that, so rsync the file instead
        OpenShiftUtils.binary().execute(
            "exec",
            "-n", TestConfiguration.openShiftNamespace(), integrationPodName, "--", "/bin/bash", "-c",
            "jar xf /deployments/project-0.1-SNAPSHOT.jar META-INF/maven/io.syndesis.integrations/project/pom.xml && " +
                "cp META-INF/maven/io.syndesis.integrations/project/pom.xml /deployments/pom.xml"
        );

        OpenShiftUtils.binary().execute(
            "rsync",
            "-n", TestConfiguration.openShiftNamespace(),
            integrationPodName + ":/deployments/pom.xml",
            "/tmp"
        );

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            return builderFactory.newDocumentBuilder().parse(FileUtils.openInputStream(new File("/tmp/pom.xml")));
        } catch (Exception e) {
            fail("Unable to parse integration's pom.xml: ", e);
        }
        return null;
    }
}
