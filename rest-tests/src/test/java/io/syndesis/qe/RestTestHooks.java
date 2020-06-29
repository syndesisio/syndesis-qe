package io.syndesis.qe;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoint.ConnectionsEndpoint;
import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.resource.impl.SyndesisDB;
import io.syndesis.qe.utils.SampleDbConnectionManager;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import io.cucumber.java.After;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 19, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class RestTestHooks {

    @Autowired
    private StepsStorage stepStorage;

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    @After("@sqs")
    public void clearSqsProperty() {
        System.clearProperty("sqs.batch");
    }

    @After
    public void afterTest() {
        stepStorage.flushStepDefinitions();
        log.debug("Flushed steps from steps storage");
        SampleDbConnectionManager.closeConnections();

        if (TestConfiguration.isDeloreanEnvironment()) {
            //delete all integrations and connections after the test. Only for Delorean since it doesn't support TEST_SUPPORT env
            List<Integration> integrations = integrationsEndpoint.list();
            for (Integration integration : integrations) {
                integrationsEndpoint.delete(integration.getId().get());
            }

            List<Connection> connections = connectionsEndpoint.list();
            for (Connection connection : connections) {
                if (SyndesisDB.DEFAULT_PSQL_CONNECTION_ORIGINAL.equals(connection.getName()) ||
                    SyndesisDB.DEFAULT_PSQL_CONNECTION_BACKUP.equals(connection.getName()) ||
                    //for default connections (Webhook, Log, Flow, Timer, Api Provider etc.) We don't want to delete them
                    connection.getTags().isEmpty()) {
                    continue;
                }
                connectionsEndpoint.delete(connection.getId().get());
            }
        }
    }

    @After("@publicapi-connections")
    public void resetPostgresDB() {
        log.info("Back default values of PostgresDB");
        TestSupport.getInstance().resetDB();
    }
}
