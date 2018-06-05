package io.syndesis.qe.endpoints;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * TestSupport class contains utility methods for usage of the test-support endpoint.
 *
 * @author jknetl
 */
@Slf4j
public final class TestSupport {

    private static final String ENDPOINT_NAME = "/test-support";
    private static final String apiPath = TestConfiguration.syndesisRestApiPath();
    private static Client client;
    private static TestSupport instance = null;

    private final AccountsDirectory accountsDirectory = AccountsDirectory.getInstance();

    private TestSupport() {
        client = RestUtils.getClient();
    }

    public static TestSupport getInstance() {
        if (instance == null) {
            instance = new TestSupport();
        }
        return instance;
    }

    /**
     * Resets Syndesis database.
     */
    public void resetDB() {
        resetDbWithResponse();
    }

    /**
     * Resets Syndesis database.
     *
     * @return HTTP response code
     */
    public int resetDbWithResponse() {
        log.info("Resetting syndesis DB.");

        final Invocation.Builder invocation = client
                .target(getEndpointUrl())
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");
        int responseCode = invocation.get().getStatus();
        log.debug("Reset endpoint reponse: {}", responseCode);
        return responseCode;
    }

    public String getEndpointUrl() {
        String restEndpoint = String.format("%s%s%s%s", RestUtils.getRestUrl(), apiPath, ENDPOINT_NAME, "/reset-db");
        log.info("Reset endpoint URL: *{}*", restEndpoint);
        return restEndpoint;
    }

    public void createPostgresDBconnection(ConnectorsEndpoint connectorsEndpoint, ConnectionsEndpoint connectionsEndpoint) {
        //connector: "sql"
        //
        Optional<Account> optAccount = accountsDirectory.getAccount("postgresdb");
        if (!optAccount.isPresent()) {
            Map<String, String> map = new HashMap<>();
            map.put("db.jdbc_url", "jdbc:postgresql://syndesis-db:5432/sampledb");
            map.put("db.username", "sampledb");
            map.put("db.password", "»ENC:de2dfd965136569351fe7adf4fd72e6342a8b2d93a1461f769981d3330d15781758252b02223d5ff429bd49f163ef822");
            map.put("db.name", "sampledb");

            TestUtils.transhipExternalProperties("postgresdb", map);
            optAccount = accountsDirectory.getAccount("postgresdb");
        }
        Assertions.assertThat(optAccount.isPresent()).isTrue();

        Account account = optAccount.get();
        final Connector dbConnector = connectorsEndpoint.get("sql");

        final Connection postgresDBConnection = new Connection.Builder()
                .connector(dbConnector)
                .connectorId(getConnectorId(dbConnector))
                .id(RestConstants.POSTGRESDB_CONNECTION_ID)
                .icon("fa-database")
                .name("PostgresDB")
                .configuredProperties(TestUtils.map(
                        "url", account.getProperty("url"),
                        "user", account.getProperty("user"),
                        "schema", account.getProperty("schema"),
                        "password", account.getProperty("password")))
                .tags(Arrays.asList("postgresdb"))
                .build();

        log.info("Creating PostgresDB connection named *{}*", postgresDBConnection.getName());
        connectionsEndpoint.create(postgresDBConnection);
    }

    //    AUXILIARIES:
    private String getConnectorId(Connector connector) {
        return connector.getId().orElseThrow(() -> new IllegalArgumentException("Connector ID is null"));
    }
}
