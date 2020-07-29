package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.ODataUtils;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

import java.util.List;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

@Slf4j
public class ODataSteps {
    @When("^.*insert entity \"([^\"]*)\" into \"([^\"]*)\" collection on OData service$")
    public void insertEntityToODataService(final String entity, final String collection) {
        String requestBody = ODataUtils.readResourceFile(this.getClass().getClassLoader().getResource(entity));
        Headers headers = Headers.of("Content-Type", "application/json");
        HTTPResponse response = HTTPUtils.doPostRequest(ODataUtils.getOpenshiftRoute() + collection, requestBody, headers);
        assertThat(response.getCode()).isEqualTo(201);
        log.info("Entity from resource file " + entity + " succesfully inserted to sample OData service");
    }

    @Then("^.*check that \"([^\"]*)\" entity in \"([^\"]*)\" collection contains$")
    public void oDataServiceContains(String entityKey, final String collection, DataTable table) {
        if (entityKey.matches("[A-Za-z]*")) {
            entityKey = "'" + entityKey + "'";
        }
        HTTPResponse response = HTTPUtils.doGetRequest(ODataUtils.getOpenshiftRoute() + collection + "(" + entityKey + ")");
        assertThat(response.getCode()).isEqualTo(200);
        for (List<String> row : table.cells()) {
            assertThat(response.getBody()).contains(row.stream().map(s -> s == null ? "" : s).collect(Collectors.toList()));
        }
    }

    @Then("^.*check that entity \"([^\"]*)\" is not present in \"([^\"]*)\" collection on OData service$")
    public void oDataServiceDoesntContain(final String entityKey, final String collection) {
        HTTPResponse response = HTTPUtils.doGetRequest(ODataUtils.getOpenshiftRoute() + collection + "(" + entityKey + ")");
        assertThat(response.getCode()).isEqualTo(404);
    }

    @Then("^.*reset OData service$")
    public void resetODataService() {
        log.info("Reseting data on sample OData service");
        HTTPResponse response = HTTPUtils.doPostRequest(ODataUtils.getOpenshiftRoute() + "Reset", "{}");
        assertThat(response.getCode()).isEqualTo(204);
    }

    @Then("^.*validate that OData service contains entity with \"([^\"]*)\":\"([^\"]*)\" property:value pair in \"([^\"]*)\" collection$")
    public void checkEntityWithPropertyExists(String propertyName, String expectedValue, String entitySetName) {
        String route = ODataUtils.getOpenshiftRoute();
        HTTPResponse response = HTTPUtils.doGetRequest(route + entitySetName);
        log.info("Checking if entity with " + propertyName + ":" + expectedValue + " property:value pair is present");
        assertThat(response.getBody()).contains("\"" + propertyName + "\":\"" + expectedValue + "\"");
    }
}
