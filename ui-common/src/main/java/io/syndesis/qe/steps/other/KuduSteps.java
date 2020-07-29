package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.http.HTTPUtils;

import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KuduSteps {

    @When("^create table in Kudu server$")
    public void createTable() {
        String response = HTTPUtils.doGetRequest("http://" + OpenShiftUtils.getInstance().getRoutes().stream()
            .filter(route -> "kudu-rest-api".equals(route.getMetadata().getName()))
            .findFirst().get().getSpec().getHost() + "/kudu/table/create").getBody();

        if (!(response.contains("Table created") || response.contains("Table my-table already exists"))) {
            fail("Table creation failed: " + response);
        }
    }

    @When("^delete table from Kudu server$")
    public void deleteTable() {
        String response = HTTPUtils.doGetRequest("http://" + OpenShiftUtils.getInstance().getRoutes().stream()
            .filter(route -> "kudu-rest-api".equals(route.getMetadata().getName()))
            .findFirst().get().getSpec().getHost() + "/kudu/table/delete").getBody();
        log.debug("Table deletion log: " + response);
    }

    @When("^insert a row into Kudu server table$")
    public void insertIntoTable() {
        String response = HTTPUtils.doGetRequest("http://" + OpenShiftUtils.getInstance().getRoutes().stream()
            .filter(route -> "kudu-rest-api".equals(route.getMetadata().getName()))
            .findFirst().get().getSpec().getHost() + "/kudu/table/insert").getBody();

        assertThat(response).isEqualToIgnoringCase("Insert successful");
    }

    @When("^check that Kudu server table contains inserted data$")
    public void validateKuduTableData() {
        String response = HTTPUtils.doGetRequest("http://" + OpenShiftUtils.getInstance().getRoutes().stream()
            .filter(route -> "kudu-rest-api".equals(route.getMetadata().getName()))
            .findFirst().get().getSpec().getHost() + "/kudu/table/validate").getBody();

        assertThat(response).isEqualToIgnoringCase("Success");
    }
}
