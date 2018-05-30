package io.syndesis.qe.rest.tests.integrations;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.When;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * June 7, 2018 Red Hat
 *
 * @author sveres@redhat.com
 */
@Slf4j
public class DropboxSteps extends AbstractStep {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    public DropboxSteps() {
    }

    @When("^create Dropbox download START action step with file path: \"([^\"]*)\"$")
    public void createDropboxDownloadAction(String filePath) {
        this.createDropboxAction("download", filePath);
    }

    @When("^create Dropbox upload FINISH action step with file path: \"([^\"]*)\"$")
    public void createDropboxUploadAction(String filePath) {
        this.createDropboxAction("upload", filePath);
    }

//    AUXILIARIES:

    private void createDropboxAction(String mode, String filePath) {
        final Connector dropboxConnector = connectorsEndpoint.get("dropbox");
        final Connection dropboxConnection = connectionsEndpoint.get(RestConstants.DROPBOX_CONNECTION_ID);

        String connectorPrefix = "";
        Map<String, String> properties = null;
        switch (mode){
            case "download":
                connectorPrefix = "io.syndesis:dropbox-download-connector";
                properties = TestUtils.map("remotePath", filePath);
                break;
            case "upload":
                connectorPrefix = "io.syndesis:dropbox-upload-connector";
                properties = TestUtils.map("remotePath", filePath, "uploadMode", "add");
                break;
            default:
                Assertions.fail("Undefined Dropbox connector action!");
        }

        final Action dropboxDownloadAction = TestUtils.findConnectorAction(dropboxConnector, connectorPrefix);
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(dropboxDownloadAction, properties, RestConstants.DROPBOX_CONNECTION_ID);

        final Step dropboxStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(dropboxConnection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(dropboxDownloadAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(dropboxStep));
    }
}
