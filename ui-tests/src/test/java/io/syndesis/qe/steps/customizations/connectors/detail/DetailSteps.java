package io.syndesis.qe.steps.customizations.connectors.detail;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import org.assertj.core.api.Assertions;

import java.util.List;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^edit property")
    public void editProperty(DataTable dataTable) {
        for (List<String> data : dataTable.raw()) {
            String propertyName = data.get(0);
            String propertyValue = data.get(1);
            String id = data.get(2);

            connectorDetailPage.getTextToEditElement(propertyName).click();
            connectorDetailPage.getTextEditor(id).setValue(propertyValue);
            connectorDetailPage.getEditablePropertyLabel(propertyName).click();
            //check value has been set
            Assertions.assertThat(connectorDetailPage.getTextToEditElement(propertyName).text()).isEqualToIgnoringCase(propertyValue);
        }
    }
}
