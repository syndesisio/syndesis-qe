package io.syndesis.qe.steps.customizations.connectors.detail;

import com.codeborne.selenide.Condition;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import org.openqa.selenium.By;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^validate connector detail values")
    public void editProperty(DataTable dataTable) {
        for (List<String> dataRow : dataTable.raw()) {
            String elementId = dataRow.get(0);
            String expectedText = dataRow.get(1);
            assertThat($(By.id(elementId)).shouldBe(Condition.visible).getAttribute("value"))
                    .containsIgnoringCase(expectedText);
        }
    }
}
