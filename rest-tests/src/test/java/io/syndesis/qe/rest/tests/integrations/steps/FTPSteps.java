package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FTPSteps extends AbstractStep {
    @When("^create FTP \"([^\"]*)\" action with values$")
    public void createFtpStep(String action, DataTable sourceMappingData) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.FTP.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.FTP.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:ftp-" + action);
        super.addProperty(StepProperty.PROPERTIES, sourceMappingData.asMaps(String.class, String.class).get(0));
        super.createStep();
    }
}
