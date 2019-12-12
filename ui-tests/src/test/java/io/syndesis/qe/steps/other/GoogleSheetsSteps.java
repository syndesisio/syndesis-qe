package io.syndesis.qe.steps.other;

import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.GoogleSheetsUtils;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSheetsSteps {

    @Autowired
    private GoogleSheetsUtils gcu;

    @When("^define spreadsheetID as property in data mapper$")
    public void defineSpreadaheetIDAsPropertyInDataMapper() {
        defineSpreadsheetIdInDatamapper(gcu.getTestSheetId());
    }

    @When("^fill spreadsheet ID$")
    public void fillSpreadsheetID() {
        new SyndesisRootPage().getElementByLocator(ByUtils.dataTestId("spreadsheetid")).sendKeys(gcu.getTestSheetId());
    }

    @Given("^clear range \"([^\"]*)\" in data test spreadsheet$")
    public void clearRangeInDataTestSpreadsheet(String range) {
        gcu.clearSpreadSheetValues(gcu.getTestDataSpreadSheet(), range);
    }

    @When("^clear test spreadsheet$")
    public void clearTestSpreadsheet() {
        gcu.clearSpreadSheetValues("A1:Z500000");
    }

    @When("define test spreadsheetID as property in data mapper")
    public void defineTestSpreadsheetIDAsPropertyInDataMapper() {
        defineSpreadsheetIdInDatamapper(gcu.getTestDataSpreadSheet());
    }

    private void defineSpreadsheetIdInDatamapper(String id) {
        DataMapper mapper = new DataMapper();
        mapper.switchToDatamapperIframe();
        mapper.addProperty("spreadsheetId", id, "String");
        mapper.switchIframeBack();
    }
}
