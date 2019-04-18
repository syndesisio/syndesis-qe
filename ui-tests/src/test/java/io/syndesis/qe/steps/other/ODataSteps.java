package io.syndesis.qe.steps.other;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class ODataSteps {

    private final String ODATA_ROUTE = "http://odata-syndesis.my-minishift.syndesis.io/TripPin/";
    private final String ODATA_SERVICE = "http://odata.syndesis.svc:8080/TripPin";
    private Account oData;
    private final String ENTITIES_FOLDER = "odata_entities/";

    @When("^create OData credentials$")
    public void createODataCredentials() {
        oData = new Account();
        oData.setService("OData");
        Map<String, String> properties = new HashMap<>();
        properties.put("serviceUri", ODATA_SERVICE);
        oData.setProperties(properties);
        AccountsDirectory.getInstance().addAccount("odata", oData);
        log.info("Created new Account: odata");
    }

    @When("^.*insert entity \"([^\"]*)\" into \"([^\"]*)\" collection on OData service$")
    public void insertEntityToODataService(final String entity, final String collection) {
        String requestBody = loadResourceFile(ENTITIES_FOLDER, entity);
        Headers headers = Headers.of("Content-Type", "application/json");
        int code = (HttpUtils.doPostRequest(ODATA_ROUTE + collection, requestBody, headers)).getCode();
        assertThat(code).isEqualTo(201);
        log.info("Entity from resource file " + entity + " succesfully inserted to sample OData service");
    }

    private String loadResourceFile(final String folder, final String file) {
        String requestBody = null;
        try {
            requestBody = new String(Files.readAllBytes(getFilePath(folder, file)));
        } catch (IOException e) {
            fail(file + " could not be loaded", e);
        }
        return requestBody;
    }

    private Path getFilePath(String folder, String file) {
        log.info("Loading resource file from odata_data folder: " + file);
        URL requestBodyURL = this.getClass().getClassLoader().getResource(folder + file);
        if (requestBodyURL == null) {
            fail("File with name " + file + " doesn't exist in the resources");
        }
        return Paths.get(requestBodyURL.getPath());
    }

    @Then("^.*check that \"([^\"]*)\" entity in \"([^\"]*)\" collection contains$")
    public void oDataServiceContains(final String entityKey, final String collection, DataTable table) {
        HTTPResponse response = getEntity(collection, entityKey);
        log.info("Request ended with code " + response.getCode());
        assertThat(response.getCode()).isEqualTo(200);
        Map<String, Object> json = (new JSONObject(response.getBody())).toMap();
        for (List<String> row : table.cells()) {
            assertThat(json).containsValues(row.toArray());
        }
    }

    private HTTPResponse getEntity(String collection, String entityKey) {
        // Currently supports only entity keys in number (integer) form
        String url = ODATA_ROUTE + collection + "(" + entityKey + ")";
        log.info("Requesting entity from: " + url);
        return HttpUtils.doGetRequest(url);
    }

    @Then("^.*check that entity \"([^\"]*)\" is not present in \"([^\"]*)\" collection on OData service$")
    public void oDataServiceDoesntContain(final String entityKey, final String collection) {
        assertThat(getEntity(collection, entityKey).getCode()).isEqualTo(404);
    }

    @Then("^.*reset OData service$")
    public void resetODataService() {
        String url = ODATA_ROUTE + "Reset";
        log.info("Reseting data on sample OData service");
        assertThat(HttpUtils.doPostRequest(url, "{}").getCode()).isEqualTo(204);
    }

    @Then("^.*validate that OData service contains entity with \"([^\"]*)\":\"([^\"]*)\" property:value pair in \"([^\"]*)\" collection$")
    public void checkEntityWithPropertyExists(String propertyName, String expectedValue, String entitySetName) {
        log.info("Getting " + ODATA_ROUTE + entitySetName);
        HTTPResponse response = HttpUtils.doGetRequest(ODATA_ROUTE + entitySetName);
        log.info("Checking if entity with " + propertyName + ":" + expectedValue + " property:value pair is present");
        assertThat(response.getBody()).contains("\"" + propertyName + "\":\"" + expectedValue + "\"");
    }

}
