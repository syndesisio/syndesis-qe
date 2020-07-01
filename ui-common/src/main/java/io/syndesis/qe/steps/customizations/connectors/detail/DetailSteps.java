package io.syndesis.qe.steps.customizations.connectors.detail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^validate connector detail values by \"([^\"]*)\"$")
    public void validateByDataTestid(String selector, DataTable dataTable) {
        this.validateConnectorDetail(selector, dataTable);
    }

    private void validateConnectorDetail(String selector, DataTable dataTable) {
        for (List<String> dataRow : dataTable.cells()) {
            String selectorValue = dataRow.get(0);
            String expectedText = dataRow.get(1);
            SelenideElement element = this.getElement(selector, selectorValue);
            assertThat(element.shouldBe(Condition.visible).getAttribute("value")).containsIgnoringCase(expectedText);
        }
    }

    private SelenideElement getElement(String selector, String value) {
        switch (selector) {
            case "id":
                return $(By.id(value));
            case "data-testid":
                return $(ByUtils.dataTestId(value));
            default:
                break;
        }
        return null;
    }
}
