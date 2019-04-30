package io.syndesis.qe.rest.tests.publicapi;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.publicendpoint.ConnectionsPublicEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConnectionsPublicApiSteps {

    @Autowired
    private ConnectionsPublicEndpoint connectionsPublicEndpoint;

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    /**
     * DataTable ->  | property1 | value |
     * | property2 | value |
     */
    @When("^update properties of connection (\\w+)$")
    public void updateConnectionProperties(String connectionName, DataTable tagsData) {
        List<List<String>> connectionProperties = tagsData.cells();
        Properties properties = new Properties();
        for (List<String> connectionProperty : connectionProperties) {
            properties.put(connectionProperty.get(0), connectionProperty.get(1));
        }
        ConnectionOverview response = connectionsPublicEndpoint.updateConnectionProperties(connectionName, properties);

        for (List<String> connectionProperty : connectionProperties) {
            assertThat(response.getConfiguredProperties()).containsEntry(connectionProperty.get(0), connectionProperty.get(1));
        }
    }

    /**
     * DataTable ->  | property1 | value |
     * | property2 | value |
     */
    @Then("^check that (\\w+) connection contains properties$")
    public void checkAllTags(String connectionName, DataTable tagsData) {
        List<List<String>> connectionProperties = tagsData.cells();
        Connection connection = connectionsEndpoint.getConnectionByName(connectionName);
        for (List<String> connectionProperty : connectionProperties) {
            assertThat(connection.getConfiguredProperties()).containsEntry(connectionProperty.get(0), connectionProperty.get(1));
        }
    }

}
