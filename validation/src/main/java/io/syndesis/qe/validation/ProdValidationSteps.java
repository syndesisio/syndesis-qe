package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.util.IntegrationPodUtil;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation methods for validating productized builds.
 */
@Slf4j
public class ProdValidationSteps {
    @Then("check that {string} integration pom contains productized version in property {string}")
    public void checkProductizedVersionIn(String integration, String property) {
        final XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            assertThat(((Node) xPath.compile("//project/properties/" + property)
                .evaluate(IntegrationPodUtil.getIntegrationXml(integration), XPathConstants.NODE)).getTextContent())
                .isEqualTo(TestConfiguration.get().readValue(property));
        } catch (XPathExpressionException e) {
            fail("Unable to compile xpath expression: ", e);
        }
    }
}
