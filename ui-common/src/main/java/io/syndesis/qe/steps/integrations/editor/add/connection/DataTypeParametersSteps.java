package io.syndesis.qe.steps.integrations.editor.add.connection;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.pages.integrations.editor.DataTypeParametersDialog;

import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataTypeParametersSteps {

    private final DataTypeParametersDialog dataTypeDialog = new DataTypeParametersDialog();

    @When("^confirm Data Type parameters dialog$")
    public void closeDataTypeDialog() {
        dataTypeDialog.closeDialog();
    }

    @When("^add Data Type parameters")
    public void addDataTypeParameter(DataTable dataTable) {
        for (List<String> dataRow : dataTable.cells()) {
            dataTypeDialog.addParameter(dataRow.get(0), dataRow.get(1));
        }
    }

    @When("delete Data Type parameter {string}")
    public void deleteDataTypeParameter(String parameterName) {
        dataTypeDialog.deleteParameter(parameterName);
    }

    @When("^update Data Type parameters")
    public void updateDataTypeParameter(DataTable dataTable) {
        for (List<String> dataRow : dataTable.cells()) {
            dataTypeDialog.updateExistedParameter(dataRow.get(0), dataRow.get(1));
        }
    }

    @Then("^verify Data Type parameters and values")
    public void verifyDataTypeParameters(DataTable dataTable) {
        assertThat(dataTypeDialog.getExistedParameters()).containsExactlyElementsOf(dataTable.column(0));
        for (List<String> dataRow : dataTable.cells()) {
            assertThat(dataTypeDialog.getBooleanValueOfParameter(dataRow.get(0))).isEqualTo(Boolean.parseBoolean(dataRow.get(1)));
        }
    }
}
