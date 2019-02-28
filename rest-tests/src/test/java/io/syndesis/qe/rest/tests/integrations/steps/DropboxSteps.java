package io.syndesis.qe.rest.tests.integrations.steps;

import java.util.Map;

import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * June 7, 2018 Red Hat
 *
 * @author sveres@redhat.com
 */
@Slf4j
public class DropboxSteps extends AbstractStep {
    @When("^create Dropbox \"([^\"]*)\" action step with file path: \"([^\"]*)\"$")
    public void createDropboxStep(String mode, String filePath) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DROPBOX.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DROPBOX.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:dropbox-" + mode);
        Map<String, String> properties = TestUtils.map("remotePath", filePath);
        if ("upload".equals(mode.toLowerCase())) {
            properties.put("uploadMode", "add");
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }
}
