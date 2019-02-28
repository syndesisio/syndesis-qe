package io.syndesis.qe.rest.tests.integrations.steps;

import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SalesforceSteps extends AbstractStep {
    @When("^create SF \"([^\"]*)\" action step with properties$")
    public void createSfStepWithActionAndProperties(String action, DataTable props) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SALESFORCE.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SALESFORCE.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, props.asMap(String.class, String.class));
        super.createStep();
    }
}
