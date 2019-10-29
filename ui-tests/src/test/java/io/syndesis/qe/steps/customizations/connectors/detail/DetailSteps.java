package io.syndesis.qe.steps.customizations.connectors.detail;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import cucumber.api.java.en.Then;
import io.cucumber.datatable.DataTable;

public class DetailSteps {

    private ApiClientConnectorDetail connectorDetailPage = new ApiClientConnectorDetail();

    @Then("^validate connector detail values by \"([^\"]*)\"$")
    public void validateByDataTestid(DataTable dataTable, String selector) {
        this.validateConnectorDetail(dataTable, selector);
    }

    private void validateConnectorDetail(DataTable dataTable, String selector) {
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
