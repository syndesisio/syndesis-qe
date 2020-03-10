package io.syndesis.qe.steps.datavirtualization;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import io.syndesis.qe.pages.Virtualizations.Virtualizations;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.utils.TestUtils;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataVirtualizationSteps {

    private Virtualizations virtualizations = new Virtualizations();

    @Autowired
    private CommonSteps commonSteps;

    @Then("check that data virtualization {string} is present in virtualizations list")
    public void expectVirtualizationPresent(String name) {
        commonSteps.navigateTo("Data");
        log.info("Verifying virtualization {} is present", name);
        TestUtils.sleepForJenkinsDelayIfHigher(4);
        assertThat(virtualizations.isVirtualizationPresent(name)).isTrue();
    }

    @Then("check that data virtualization {string} has description {string}")
    public void checkThatDataVirtualizationHasDescription(String virtualization, String description) {
        commonSteps.navigateTo("Data");
        log.info("Verifying description {} for virtualization {}", description, virtualization);
        TestUtils.sleepForJenkinsDelayIfHigher(4);
        assertThat(virtualizations.getVirtualizationDescription(virtualization)).as("Description should be {} but it is {}", description)
            .isEqualTo(description);
    }

    @When("edit data virtualization {string}")
    public void editDataVirtualization(String name) {
        virtualizations.editVirtualization(name);
    }

    @Then("check that data virtualization view {string} is present in view list")
    public void expectViewPresent(String name) {
        log.info("Verifying virtualization view {} is present", name);
        TestUtils.sleepForJenkinsDelayIfHigher(4);
        assertThat(virtualizations.isVirtualizationPresent(name)).isTrue();
    }

    @When("go to {string} tab on virtualization page")
    public void goToTabOnVirtualizationPage(String tabName) {
        virtualizations.openTab(tabName);
    }

    @When("set parameters for SQL client")
    public void setParametersForSQLClient(DataTable table) {
        virtualizations.setSQLclientParams(table);
    }

    @Then("check that number of rows for query is {int} in sql client")
    public void checkThatNumberOfRowsForQueryIsInSqlClient(int number) {
        int rows = virtualizations.getNumberofRowsSqlClient();
        assertThat(rows).as("Number of returned rows should be %s but is %s", number, rows).isEqualTo(number);
    }

    @When("make action {string} on the virtualization {string}")
    public void makeActionOnTheVirtualization(String action, String virtualization) {
        commonSteps.navigateTo("Data");
        virtualizations.makeActionOnVirtualization(virtualization, action);
    }

    /*
     * State != starting state
     * State is state of integration. e.g. "Running", "Stopped"
     */
    @Then("^wait until virtualization \"([^\"]*)\" gets into \"([^\"]*)\" state$")
    public void waitForIntegrationState(String virtualizationName, String virtualizationStatus) {
        commonSteps.navigateTo("Data");
        SelenideElement virtualization = virtualizations.getVirtualization(virtualizationName);
        TestUtils.sleepForJenkinsDelayIfHigher(10);
        assertThat(TestUtils.waitForEvent(
            status -> status.contains(virtualizationStatus),
            () -> virtualizations.getVirtualizationItemStatus(virtualization),
            TimeUnit.MINUTES, 10, TimeUnit.SECONDS, 20)
        ).isTrue();
    }

    @When("edit data virtualization view {string}")
    public void editDataVirtualizationView(String viewName) {
        virtualizations.editView(viewName);
    }

    /**
     * @param page if "import" then import page otherwise create page
     */
    @When("select DV connection {string} on {string} page")
    public void selectDVConnection(String connectionName, String page) {
        final String CONNECTION_CARD = "*[data-testid=\"dv-connection-card-%s-card\"]";
        final String CONNECTION_SCHEMA = "*[data-testid=\"connection-schema-list-item-%s-list-item\"]";

        String element;
        if ("import".equals(page)) {
            element = CONNECTION_CARD;
        } else {
            element = CONNECTION_SCHEMA;
        }

        $(By.cssSelector(String.format(element,
            connectionName.toLowerCase()
                .replaceAll("\\s|\\)", "-")
                .replaceAll("\\(", "")))
        ).click();
    }

    @When("select DV connection tables on create page")
    public void selectDVConnectionTablesCreate(DataTable table) {
        final String TABLE_CREATE = "*[data-testid=\"schema-node-list-item-%s-list-item\"]";

        for (List<String> dataRow : table.cells()) {
            SelenideElement tableElement = $(By.cssSelector(String.format(TABLE_CREATE,
                dataRow.get(0).toLowerCase()
                    .replaceAll("\\s|\\)", "-")
                    .replaceAll("\\(", ""))));
            tableElement.$("input").click();
        }
    }

    @When("select DV connection tables on import page")
    public void selectDVConnectionTablesImport(DataTable table) {       //TODO possible join with method above in the future
        for (List<String> dataRow : table.cells()) {
            ElementsCollection tables = $$("td").filter(Condition.attribute("data-key", "2"));
            //Wait 1 sec because of Chrome
            TestUtils.sleepIgnoreInterrupt(1000);

            for (SelenideElement tableElement : tables) {
                String[] tmp = tableElement.getText().split("/");
                if (tmp[tmp.length - 1].equals(dataRow.get(0))) {
                    tableElement.parent().$("input").click();
                    break;
                }
            }
        }
    }

    @Then("check that ddl exists and contains text {string}")
    public void checkThatDdlExistsAndContainText(String text) {
        String editorText = $(By.className("ddl-editor")).$(By.className("text-editor")).getText();
        assertThat(editorText.contains(text)).as("DDL editor does not contain text: %s", text).isTrue();
    }

    @Then("check that number of rows for preview is {int} at view editor page")
    public void checkThatNumberOfRowsForPreviewIsAtViewEditorPage(int rows) {
        String infoText = $(By.className("expandable-preview__section")).$("small").getText();

        String[] found = infoText.split(" ");
        if (found.length != 4) {
            throw new IllegalArgumentException(String.format("failed to get files number from %s", infoText));
        }
        int results = Integer.parseInt(found[3]);
        assertThat(results).as("Preview shows %s results but it should shows %s", results, rows).isEqualTo(rows);
    }

    @When("create an invalid view and check that error appears")
    public void createAnInvalidViewAndCheckThatErrorAppears() {
        final String SAVE_BUTTON = "*[data-testid=\"ddl-editor-save-button\"]";
        SelenideElement editor = $(By.className("ddl-editor"));
        editor.$(By.className("CodeMirror")).click();
        editor.$("textarea").sendKeys("Lorem Ipsum");
        editor.$(By.cssSelector(SAVE_BUTTON)).click();

        TestUtils.sleepIgnoreInterrupt(1000);

        ElementsCollection dangers = editor.$$(By.className("pf-m-danger"));
        assertThat(dangers.size()).as("Error does not appear").isEqualTo(1);
    }
}
