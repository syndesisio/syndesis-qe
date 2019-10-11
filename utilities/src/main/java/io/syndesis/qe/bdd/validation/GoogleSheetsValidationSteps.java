package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.GoogleSheetsUtils;
import io.syndesis.qe.utils.JMSUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.services.sheets.v4.model.EmbeddedChart;
import com.google.api.services.sheets.v4.model.Sheet;

import java.util.List;

import cucumber.api.java.en.Then;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSheetsValidationSteps {
    @Autowired
    GoogleSheetsUtils sheetsUtils;

    @Then("^verify that spreadsheet was created$")
    public void verifyThatSpreadsheetWasCreated() {
        if (sheetsUtils.getTestSheetId() == null || "".equals(sheetsUtils.getTestSheetId())) {
            sheetsUtils.setTestSheetId(JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, "sheets").split(":")[1].split("\"")[1]);
            log.info("Spreadsheet ID " + sheetsUtils.getTestSheetId());
        }
        assertThat(sheetsUtils.spreadSheetExists());
    }

    @Then("^verify that test sheet contains values on range \"([^\"]*)\"$")
    public void verifyThatTestSheetContainsValuesOnRange(String range, DataTable table) {
        verifySpreadsheetContainsValuesOnRange(sheetsUtils.getTestSheetId(), range, table);
    }

    @Then("^verify that data test sheet contains values on range \"([^\"]*)\"$")
    public void verifyThatDataTestSheetContainsValuesOnRange(String range, DataTable table) {
        verifySpreadsheetContainsValuesOnRange(sheetsUtils.getTestDataSpreadSheet(), range, table);
    }

    public void verifySpreadsheetContainsValuesOnRange(String id, String range, DataTable table) {
        List result = sheetsUtils.getSpreadSheetValues(id, range);
        table.asLists().forEach(list -> {
            assertThat(result).contains(list.toString());
        });
    }

    @Then("^verify that chart was created$")
    public void verifyThatChartWasCreated() {
        for (Sheet s : sheetsUtils.getSheetsFromDataSpreadsheet()) {
            if (s.getProperties().getTitle().contains("Chart")) {
                assertThat(s.getCharts().size()).isGreaterThan(0);
                EmbeddedChart ch = s.getCharts().get(0);
                assertThat(ch.getChartId()).isGreaterThan(0);
                sheetsUtils.clearSheetInDataSpreadsheet(s.getProperties().getSheetId());
            }
        }
    }

    @Then("^verify that spreadsheet title match \"([^\"]*)\"$")
    public void verifyThatSpreadsheetTitleMatch(String title) {
        assertThat(title).isEqualTo(sheetsUtils.getSpreadSheet().getProperties().getTitle());
    }

    @Then("^verify that message from \"([^\"]*)\" queue contains \"([^\"]*)\"$")
    public void verifyThatMessageFromQueueContains(String queue, String content) {
        String text = JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queue);
        for (String s : content.split(",")) {
            assertThat(text).contains(s);
        }
    }
}
