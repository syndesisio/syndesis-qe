package io.syndesis.qe.steps.apiprovider;

import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;

import com.codeborne.selenide.SelenideElement;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiProviderFlowSteps {

    private static final String ERROR_FORMAT = "errorresponsecodes-%s-error";
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("\\d{3} .*");

    /**
     * Assumes that the page is already on the proper step configuration
     * Expects following datatable (following the petstore example):
     * | <error type>         | <value>           |
     * | Server error         | 400               |
     * | SQL Entity Not Found | 404 Pet not found |
     * It's fine to use both just error codes or error codes with description
     */
    @When("map Api Provider errors")
    public void mapErrors(DataTable table) {
        Map<String, String> data = table.asMap(String.class, String.class);
        data.forEach((error, response) -> {
            String normalizedId = error.replace(' ', '-').toLowerCase();
            //In case of Server error the id used in UI is server-error not server-error-error
            if (normalizedId.endsWith("-error")) {
                normalizedId = normalizedId.replace("-error", "");
            }
            SelenideElement dropdown = $(ByUtils.dataTestId(String.format(ERROR_FORMAT, normalizedId)));
            Matcher matcher = ERROR_CODE_PATTERN.matcher(response);
            if (matcher.matches()) {
                //Matching by error description
                dropdown.selectOption(response);
            } else {
                //Matching just by error code
                dropdown.selectOptionByValue(response);
            }
        });
    }
}
