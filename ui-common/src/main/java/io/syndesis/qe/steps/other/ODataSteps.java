package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.utils.ODataUtils;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class ODataSteps {
    @When("^.*insert entity \"([^\"]*)\" into \"([^\"]*)\" collection on OData service$")
    public void insertEntityToODataService(final String entity, final String collection) {
        String requestBody = ODataUtils.readResourceFile(this.getClass().getClassLoader().getResource(entity));
        Headers headers = Headers.of("Content-Type", "application/json");
        HTTPResponse response = HTTPUtils.doPostRequest(ODataUtils.getV4OpenshiftRoute() + collection, requestBody, headers);
        assertThat(response.getCode()).isEqualTo(201);
        log.info("Entity from resource file " + entity + " succesfully inserted to sample OData service");
    }

    @Then("^.*check that OData( V2)? \"([^\"]*)\" entity in \"([^\"]*)\" collection contains$")
    public void oDataServiceContains(String v2, String entityKey, final String collection, DataTable table) {
        if (entityKey.matches("[A-Za-z]*")) {
            entityKey = "'" + entityKey + "'";
        }

        String route;

        if (v2 != null && !v2.isEmpty()) {
            route = ODataUtils.getCurrentV2Url();
        } else {
            route = ODataUtils.getV4OpenshiftRoute();
        }

        HTTPResponse response = HTTPUtils.doGetRequest(route + collection + "(" + entityKey + ")");
        assertThat(response.getCode()).isEqualTo(200);
        for (List<String> row : table.cells()) {
            assertThat(response.getBody()).contains(row.stream().map(s -> s == null ? "" : s).collect(Collectors.toList()));
        }
    }

    @Then("^.*check that entity \"([^\"]*)\" is not present in \"([^\"]*)\" collection on OData( V2)? service$")
    public void oDataServiceDoesntContain(final String entityKey, final String collection, String v2) {
        HTTPResponse response = HTTPUtils.doGetRequest(
            (v2 != null && !v2.isEmpty() ? ODataUtils.getCurrentV2Url() : ODataUtils.getV4OpenshiftRoute()) + collection + "(" + entityKey + ")");
        assertThat(response.getCode()).isEqualTo(404);
    }

    @Then("^.*reset OData v4 service$")
    public void resetODataV4Service() {
        log.info("Reseting data on sample OData service");
        HTTPResponse response = HTTPUtils.doPostRequest(ODataUtils.getV4OpenshiftRoute() + "Reset", "{}");
        assertThat(response.getCode()).isEqualTo(204);
    }

    @Then("^.*reset OData v2 service$")
    public void resetODataV2Service() throws IOException {
        log.info("Reseting odata service by getting new user token");

        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
        Response response = client.newCall(new Request.Builder().url(ODataUtils.getV2ResetUrl()).get().build()).execute();
        assertThat(response.code()).isEqualTo(302);

        String location = response.headers().get("Location");
        if (location != null) {
            String newUrl = ODataUtils.getV2BaseUrl() + location;
            Account a = AccountsDirectory.getInstance().get("odata V2");
            Map<String, String> properties = Collections.singletonMap(
                "serviceUri",
                newUrl);
            a.setProperties(properties);
            log.info("new OData V2 URL is {}", newUrl);
        }
        response.close();
    }

    @Then("^.*validate that OData( V2)? service contains entity with \"([^\"]*)\":\"([^\"]*)\" property:value pair in \"([^\"]*)\" collection$")
    public void checkEntityWithPropertyExists(String v2, String propertyName, String expectedValue, String entitySetName) {
        String route;

        if (v2 != null && !v2.isEmpty()) {
            route = ODataUtils.getCurrentV2Url();
        } else {
            route = ODataUtils.getV4OpenshiftRoute();
        }

        HTTPResponse response = HTTPUtils.doGetRequest(route + entitySetName);
        log.info("Checking if entity with " + propertyName + ":" + expectedValue + " property:value pair is present");

        if (v2 != null && !v2.isEmpty()) {
            // v2 service returns xml file
            assertThat(response.getBody()).contains(propertyName + ">" + expectedValue);
        } else {
            // v4 service returns json file
            assertThat(response.getBody()).contains("\"" + propertyName + "\":\"" + expectedValue + "\"");
        }
    }
}
