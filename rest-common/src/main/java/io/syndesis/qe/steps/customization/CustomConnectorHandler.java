package io.syndesis.qe.steps.customization;

import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.qe.endpoint.CustomApiEndpoint;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 29, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class CustomConnectorHandler {

    @Autowired
    private CustomApiEndpoint customApiEndpoint;

    @Given("^create API connector with basic auth using file: \"([^\"]*)\"")
    public void createCustomConnectorStepFromFileBasic(String templateName) throws IOException {
        //TODO(tplevko): tbd
    }

    @Given("^create API connector with oauth auth using file: \"([^\"]*)\" and token endpoint: \"([^\"]*)\"")
    public void createCustomConnectorStepFromFileOauth(String templateName, String tokenUrl) throws IOException {
        //TODO(tplevko): tbd
    }

    @Given("^create API connector with basic auth using url: \"([^\"]*)\"")
    public void createCustomConnectorStepFromUrlBasic(String url) throws IOException {
        ConnectorSettings cs = new ConnectorSettings.Builder()
                .connectorTemplateId("swagger-connector-template")
                .configuredProperties(TestUtils.map("specification", url,
                        "authenticationType", "basic",
                        "host", inspectSwaggerJson(url, "host"),
                        "basePath", inspectSwaggerJson(url, "basePath")
                ))
                .build();
        customApiEndpoint.createCAEUsingUrl(cs);
    }

    @Given("^create API connector with ouath auth using url: \"([^\"]*)\" and token endpoint: \"([^\"]*)\"")
    public void createCustomConnectorStepFromUrlOauth(String url, String tokenUrl) throws IOException {
        ConnectorSettings cs = new ConnectorSettings.Builder()
                .connectorTemplateId("swagger-connector-template")
                .configuredProperties(TestUtils.map("specification", url,
                        "authenticationType", "oauth2",
                        "authorizationEndpoint", inspectSwaggerJson(url, "authorizationUrl"),
                        "tokenEndpoint", tokenUrl,
                        "host", inspectSwaggerJson(url, "host"),
                        "basePath", inspectSwaggerJson(url, "basePath")
                ))
                .build();
        customApiEndpoint.createCAEUsingUrl(cs);
    }

    private String inspectSwaggerJson(String url, String jsonObjectName) {
        JsonParser jp = null;
        String result = null;
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JsonFactory f = new MappingJsonFactory();
            jp = f.createParser(jsonText);
            while (!jp.isClosed()) {
                if (jsonObjectName.equals(jp.getCurrentName())) {
                    result = jp.getValueAsString();
                }
                jp.nextToken();
            }
        } catch (IOException ex) {
            log.error("Error: " + ex);
        }
        return result;
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
