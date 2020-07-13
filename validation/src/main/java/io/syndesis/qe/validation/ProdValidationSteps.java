package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;

import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation methods for validating productized builds.
 */
@Slf4j
public class ProdValidationSteps {
    private Document pom;

    private void loadIntegrationXml() {
        if (pom != null) {
            return;
        }

        final String integrationPodName = OpenShiftUtils.getPodByPartialName("i-").get().getMetadata().getName();
        log.info(integrationPodName);
        // It adds quotes around the command for exec and oc client doesn't understand that, so rsync the file instead
        OpenShiftUtils.binary().execute(
            "rsync",
            "-n", TestConfiguration.openShiftNamespace(),
            integrationPodName + ":/tmp/src/pom.xml",
            "/tmp"
        );

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            pom = builderFactory.newDocumentBuilder().parse(FileUtils.openInputStream(new File("/tmp/pom.xml")));
        } catch (Exception e) {
            fail("Unable to parse integration's pom.xml: ", e);
        }
    }

    @Then("check that integration pom contains productized version in property {string}")
    public void checkProductizedVersionIn(String property) {
        loadIntegrationXml();
        final XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            assertThat(((Node) xPath.compile("//project/properties/" + property).evaluate(pom, XPathConstants.NODE)).getTextContent())
                .isEqualTo(TestConfiguration.get().readValue(property));
        } catch (XPathExpressionException e) {
            fail("Unable to compile xpath expression: ", e);
        }
    }
}
