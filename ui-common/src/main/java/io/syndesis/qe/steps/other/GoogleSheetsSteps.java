package io.syndesis.qe.steps.other;

import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.integrations.editor.add.steps.DataMapper;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.GoogleSheetsUtils;

import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSheetsSteps {

    @Autowired
    private GoogleSheetsUtils gcu;

    @When("define spreadsheetID as constant in data mapper and map it to {string}")
    public void defineSpreadaheetIDAsPropertyInDataMapper(String mapTo) {
        defineSpreadsheetIdInDatamapper(gcu.getTestSheetId(), mapTo);
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

    @When("define test spreadsheetID as constant in data mapper and map it to {string}")
    public void defineTestSpreadsheetIDAsPropertyInDataMapper(String mapTo) {
        defineSpreadsheetIdInDatamapper(gcu.getTestDataSpreadSheet(), mapTo);
    }

    private void defineSpreadsheetIdInDatamapper(String id, String mapTo) {
        DataMapper mapper = new DataMapper();
        mapper.addConstant(id, "String");
        mapper.openDataMapperCollectionElement();
        mapper.doCreateMapping(id, mapTo);
    }
}
