package io.syndesis.qe.steps.customizations.connectors.detail;

import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^edit property")
    public void editProperty(String userName, DataTable dataTable) {
        for (List<String> data : dataTable.raw()) {
            String propertyName = data.get(0);
            String propertyValue = data.get(1);
            String id = data.get(2);

            connectorDetailPage.getTextToEditElement(propertyName).click();
            connectorDetailPage.getTextEditor(id).setValue(propertyValue);
            connectorDetailPage.getEditablePropertyLabel(propertyName).click();
            //check value has been set
            connectorDetailPage.getTextToEditElement(propertyName).text().equals(propertyValue);
        }
    }
}
