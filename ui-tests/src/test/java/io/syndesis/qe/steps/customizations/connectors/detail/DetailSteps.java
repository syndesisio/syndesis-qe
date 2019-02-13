package io.syndesis.qe.steps.customizations.connectors.detail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;

import java.util.List;

import cucumber.api.java.en.Then;
import io.cucumber.datatable.DataTable;
import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^validate connector detail values")
    public void editProperty(DataTable dataTable) {
        for (List<String> dataRow : dataTable.cells()) {
            String elementId = dataRow.get(0);
            String expectedText = dataRow.get(1);
            assertThat($(By.id(elementId)).shouldBe(Condition.visible).getAttribute("value"))
                    .containsIgnoringCase(expectedText);
        }
    }
}
