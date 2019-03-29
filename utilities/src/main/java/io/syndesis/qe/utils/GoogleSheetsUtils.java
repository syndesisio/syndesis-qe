package io.syndesis.qe.utils;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GoogleSheetsUtils {
    @Autowired
    private GoogleAccounts accounts;
    private Sheets sheets;
    @Getter
    @Setter
    private String testSheetId = "";
    @Getter
    @Setter
    private String testDataSpreadSheet = "1_OLTcj_y8NwST9KHhg8etB10xr6t3TrzaFXwW2dhpXw";

    private Sheets getSheets() {
        if (sheets == null) {
            sheets = accounts.getGoogleAccountForTestAccount("QE Google Sheets").sheets();
        }
        return sheets;
    }
    public List<String> getSpreadSheetValues() {
        return getSpreadSheetValues(testSheetId, "A1:E3");
    }

    public void clearSpreadSheetValues(String range) {
        clearSpreadSheetValues(testSheetId, range);
    }

    public void clearSpreadSheetValues(String id, String range) {
        ClearValuesRequest clear = new ClearValuesRequest();
        try {
            getSheets().spreadsheets().values().clear(id, range, clear).execute();
        } catch (IOException e) {
            Assertions.fail("clear spreadsheet values shouldn't throw any exception: " + e.getMessage());
        }
    }

    public List<String> getSpreadSheetValues(String id, String range) {
        List<String> result = new ArrayList<>();
        try {
            ValueRange response = getSheets().spreadsheets().values()
                    .get(id, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.info("No data found.");
            } else {
                for (List row : values) {
                    result.add(row.toString());
                }
            }
        } catch (IOException e) {
            Assertions.fail("get spreadsheet values shouldn't throw any exception: " + e.getMessage());
        }

        log.info("Spreadsheet values " + result.toString());
        return result;
    }

    public List<Sheet> getSheets(String id) {
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = getSheets().spreadsheets().get(id).execute();
        } catch (IOException e) {
            Assertions.fail("get sheets values shouldn't throw any exception: " + e.getMessage());
        }
        return spreadsheet.getSheets();
    }

    public void clearSheet(String spreadsheetId, int id) {
        DeleteSheetRequest clear = new DeleteSheetRequest();
        clear.setSheetId(id);

        BatchUpdateSpreadsheetRequest req = new BatchUpdateSpreadsheetRequest();
        req.setRequests(new ArrayList<>());
        req.getRequests().add(new Request().setDeleteSheet(clear));

        try {
            getSheets().spreadsheets().batchUpdate(spreadsheetId, req).execute();
        } catch (IOException e) {
            Assertions.fail("clear sheet shouldn't throw any exception: " + e.getMessage());
        }
    }

    public Spreadsheet getSpreadSheet(String id) {
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = getSheets().spreadsheets().get(id).execute();
        } catch (IOException e) {
            Assertions.fail("get spreadsheet shouldn't throw any exception: " + e.getMessage());
        }
        return spreadsheet;
    }

    public List<String> getSpreadSheetValues(String range) {
        return getSpreadSheetValues(testSheetId, range);
    }

    public List<Sheet> getSheetsFromDataSpreadsheet() {
        return getSheets(testDataSpreadSheet);
    }

    public void clearSheetInDataSpreadsheet(Integer sheetId) {
        clearSheet(testDataSpreadSheet, sheetId);
    }

    public Spreadsheet getSpreadSheet() {
        return getSpreadSheet(testSheetId);
    }
}
