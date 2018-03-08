package io.syndesis.qe.rest.tests.integrations;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpSteps {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    public FtpSteps() {
    }

    @And("^creates start FTP download action with values$")
    public void setFtpDownloadData(DataTable sourceMappingData) {

        final Connection ftpConnection = connectionsEndpoint.get(getFtpConnectionId());
        final Connector ftpConnector = connectorsEndpoint.get("ftp");

        Map<String, String> dataMap = sourceMappingData.asMaps(String.class, String.class).get(0);

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(ftpConnection)
                .action(TestUtils.findConnectorAction(ftpConnector, "io.syndesis:ftp-download-connector"))
                .configuredProperties(TestUtils.map(dataMap))
                .build();
        steps.getSteps().add(dbStep);
    }

    @And("^creates finish FTP upload action with values$")
    public void setFtpUploadData(DataTable sourceMappingData) {

        final Connection ftpConnection = connectionsEndpoint.get(getFtpConnectionId());
        final Connector ftpConnector = connectorsEndpoint.get("ftp");

        Map<String, String> dataMap = sourceMappingData.asMaps(String.class, String.class).get(0);

        final Step dbStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(ftpConnection)
                .action(TestUtils.findConnectorAction(ftpConnector, "io.syndesis:ftp-upload-connector"))
                .configuredProperties(TestUtils.map(dataMap))
                .build();
        steps.getSteps().add(dbStep);
    }

//    AUXILIARIES:
    private String getFtpConnectionId() {
        return connectionsEndpoint.list().stream().filter(s -> s.getName().equals("Ftp")).findFirst().get().getId().get();
    }
}
