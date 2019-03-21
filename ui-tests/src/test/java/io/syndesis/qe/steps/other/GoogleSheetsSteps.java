package io.syndesis.qe.steps.other;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.utils.GoogleSheetsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class GoogleSheetsSteps {

    @Autowired
    private GoogleSheetsUtils gcu;

    @When("^define spreadsheetID as property in data mapper$")
    public void defineSpreadaheetIDAsPropertyInDataMapper() {
        DataMapper mapper = new DataMapper();
        mapper.addProperty("spreadsheetId", gcu.getTestSheetId(), "String");
    }

    @When("^fill spreadsheet ID$")
    public void fillSpreadsheetID() {
        new SyndesisRootPage().getRootElement().findElementById("spreadsheetId").sendKeys(gcu.getTestSheetId());
    }

    @Given("^clear range \"([^\"]*)\" in data test spreadsheet$")
    public void clearRangeInDataTestSpreadsheet(String range) {
        gcu.clearSpreadSheetValues(gcu.getTestDataSpreadSheet(), range);
    }

    @When("^clear test spreadsheet$")
    public void clearTestSpreadsheet() {
        gcu.clearSpreadSheetValues("A1:Z500000");
    }
}
