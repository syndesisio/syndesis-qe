package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTPSteps extends AbstractStep {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    @When("^create start FTP download action with values$")
    public void setFtpDownloadData(DataTable sourceMappingData) {
        final Connector ftpConnector = connectorsEndpoint.get(RestTestsUtils.Connector.FTP.getId());
        final Connection ftpConnection = connectionsEndpoint.get(RestTestsUtils.Connection.FTP.getId());

        List<Map<String, String>> dataMap = sourceMappingData.asMaps(String.class, String.class);

        final Step ftpStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(ftpConnection)
                .id(UUID.randomUUID().toString())
                .action(TestUtils.findConnectorAction(ftpConnector, "io.syndesis:ftp-download-connector"))
                .configuredProperties(dataMap.get(0))
                .build();
        steps.getStepDefinitions().add(new StepDefinition(ftpStep));
    }

    @When("^create finish FTP upload action with values$")
    public void setFtpUploadData(DataTable sourceMappingData) {
        final Connector ftpConnector = connectorsEndpoint.get(RestTestsUtils.Connector.FTP.getId());
        final Connection ftpConnection = connectionsEndpoint.get(RestTestsUtils.Connection.FTP.getId());

        List<Map<String, String>> dataMap = sourceMappingData.asMaps(String.class, String.class);

        final Step ftpStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(ftpConnection)
                .id(UUID.randomUUID().toString())
                .action(TestUtils.findConnectorAction(ftpConnector, "io.syndesis:ftp-upload-connector"))
                .configuredProperties(dataMap.get(0))
                .build();
        steps.getStepDefinitions().add(new StepDefinition(ftpStep));
    }
}
