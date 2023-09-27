package io.syndesis.qe.steps.integrations.editor;

import static org.junit.Assert.assertEquals;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.integrations.editor.Editor;
import io.syndesis.qe.pages.integrations.editor.add.ChooseConnection;
import io.syndesis.qe.pages.integrations.fragments.IntegrationFlowView;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EditorSteps {

    private Editor editor = new Editor();
    private IntegrationFlowView flowViewComponent = new IntegrationFlowView();
    private ChooseConnection chooseConnection = new ChooseConnection();

    private static final class Element {
        public static final By EXPANDER = By.xpath(".//button[contains(@class, 'toggle-collapsed')]");
        public static final By HIDDEN_DETAILED_VIEW = By.cssSelector("div[class*='flow-view-container syn-scrollable--body collapsed']");
        public static final By INTEGRATION_EDITOR_STEPS_LIST = By.className("integration-editor-steps-list");
        public static final By INTEGRATION_EDITOR_STEPS_LIST_ITEM = ByUtils.containsDataTestId("integration-editor-steps-list-item");
        public static final By STEP_DATA_SHAPE = ByUtils.dataTestId("editor-step-datashape");
        public static final By ADDITIONAL_INFO = ByUtils.dataTestId("editor-step-info");
    }

    private static final class Button {
        public static final By PUBLISH = By.id("integration-editor-publish-button");
        public static final By SAVE_AS_DRAFT = By.id("integration-editor-save-button");
        public static final By CANCEL = By.id("integration-editor-cancel-button");
        public static final By CONFIGURE = ByUtils.dataTestId("integration-editor-step-adder-configure-button");
    }

    @When("^add data mapper step before \"([^\"]*)\" action$")
    public void addDataMapperStepBeforeAction(String actionId) {
        flowViewComponent.addDatamapperStep(actionId);
    }

    @Then("^check visibility of visual integration editor$")
    public void verifyNewIntegrationEditorOpened() {
        editor.getRootElement().shouldBe(visible);
        chooseConnection.getRootElement().shouldBe(visible);
        flowViewComponent.getRootElement().shouldBe(visible);
    }

    @Then("^check visibility of visual integration editor for \"([^\"]*)\"$")
    public void verifyEditorOpenedFor(String integrationName) {
        this.verifyNewIntegrationEditorOpened();
        log.info("editor must display integration name {}", integrationName);
        assertThat(flowViewComponent.getIntegrationName()).isEqualTo(integrationName);
    }

    @Deprecated
    @When("^add first step between START and STEP connection$")
    public void sheAddsFirstStep() {
        flowViewComponent.clickAddStepLink(0);
    }

    @Deprecated
    @When("^add second step between STEP and FINISH connection$")
    public void sheAddsSecond() {
        flowViewComponent.clickAddStepLink(1);
    }

    @When("^.*adds? integration step on position \"([^\"]*)\"$")
    public void addAnotherStep(int stepPos) {
        flowViewComponent.clickAddStepLink(stepPos);
    }

    @Then("^verify delete button on step ([0-9]+) (is|is not) visible$")
    public void verifyDeleteButtonOnStartFinishStepNotVisible(int position, String visibility) {
        SelenideElement trashIcon = flowViewComponent.getStepOnPosition(position).$(By.className("fa-trash"));
        assertEquals("Delete icon should" + visibility.replace("is", "") + " be visible",
            "is".equals(visibility),
            trashIcon.exists()
        );
    }

    /**
     * unused
     */
    @Then("^.*checks? that text \"([^\"]*)\" is \"([^\"]*)\" in hover table over \"([^\"]*)\" step$")
    public void checkTextInHoverTable(String text, String isVisible, String stepPosition) {
        if ("visible".equalsIgnoreCase(isVisible)) {
            assertThat(flowViewComponent.checkTextInHoverTable(stepPosition))
                .isNotEmpty()
                .containsIgnoringCase(text);
        } else {
            assertThat(flowViewComponent.checkTextInHoverTable(stepPosition))
                .isNotEmpty()
                .doesNotContain(text);
        }
    }

    /**
     * Every step element has class step and every option to add step/connection also has class step.
     * So if you have 3 steps created, stepPosition is: first = 0, second = 2, third = 4 etc.
     *
     * @param text
     * @param isVisible
     * @param stepPosition
     */
    @Then("^.*checks? that text \"([^\"]*)\" is \"([^\"]*)\" in step warning inside of step number \"([^\"]*)\"$")
    public void checkTextInStepWarning(String text, String isVisible, int stepPosition) {
        if ("visible".equalsIgnoreCase(isVisible)) {
            doCheckTextInStepsWarningTable(text, stepPosition, true);
        } else {
            doCheckTextInStepsWarningTable(text, stepPosition, false);
        }
    }

    @Then("^.*checks? that text \"([^\"]*)\" is \"([^\"]*)\" in step warning inside of steps$")
    public void checkTextInStepsWarning(String text, String isVisible, DataTable table) {
        for (String index : table.asList()) {
            if ("visible".equalsIgnoreCase(isVisible)) {
                doCheckTextInStepsWarningTable(text, Integer.valueOf(index), true);
            } else {
                doCheckTextInStepsWarningTable(text, Integer.valueOf(index), false);
            }
        }
    }

    public void doCheckTextInStepsWarningTable(String text, int position, boolean visible) {
        if (visible) {
            assertThat(flowViewComponent.getWarningTextFromStep(position))
                .isNotEmpty()
                .containsIgnoringCase(text);
        } else {
            assertThat(flowViewComponent.getWarningTextFromStep(position))
                .isNotEmpty()
                .doesNotContain(text);
        }
    }

    @Then("^.*checks? that in connection info popover for step number \"([^\"]*)\" is following text$")
    public void checkTextInConnectionInfo(int stepPosition, DataTable connectionsData) {
        List<String> data = connectionsData.transpose().asList(String.class);
        String foundText = flowViewComponent.getConnectionPropertiesText(flowViewComponent.getStepOnPosition(stepPosition));

        assertThat(foundText).isNotEmpty();

        for (String column : data) {
            assertThat(foundText)
                .containsIgnoringCase(column);
        }
    }

    /**
     * Every step element has class step and every option to add step/connection also has class step.
     * If you have 3 steps created, position is: first = 0, second = 2, third = 4 etc.
     *
     * @param position index of element with class .step
     */
    @Then(".*checks? that there is no warning inside of step number \"([^\"]*)\"$")
    public void checkIfWarningIsVisible(int position) {
        assertThat(flowViewComponent.getStepWarningElement(position).isDisplayed()).isFalse();
    }

    @Then(".*checks? that there is no warning inside of steps in range from \"([^\"]*)\" to \"([^\"]*)\"$")
    public void checkIfWarningIsVisibleInRange(int start, int finish) {
        for (int i = start; i <= finish; i++) {
            assertThat(flowViewComponent.getStepWarningElement(i).isDisplayed()).isFalse();
        }
    }

    @When("^open integration flow details")
    public void openIntegrationFlowDetails() {
        try {
            OpenShiftWaitUtils.waitFor(() -> editor.getRootElement().$(Element.HIDDEN_DETAILED_VIEW).exists(), 15 * 1000);
            log.info("Expander is closed, opening details");
            editor.getRootElement().$(Element.EXPANDER).shouldBe(visible).click();
        } catch (InterruptedException | TimeoutException e) {
            log.warn("Expander to open was not found!");
        }
    }

    @Then("^check flow title is \"([^\"]*)\"$")
    public void checkFlowTitleIs(String title) {
        //As the log says, the flow title is not displayed in the editor
        //Only way to know what the flow title is by looking at the breadcrumbs or operation list
        //This is reported as #6162
        log.warn("Operations don't have titles displayed until #6162 gets resolved");
        //assertEquals("Wrong flow title", title, flowViewComponent.getFlowTitle());
    }

    @Then("^check there are (\\d+) integration steps$")
    public void checkNumberOfIntegrationSteps(int n) {
        TestUtils.waitFor(() -> $(Element.INTEGRATION_EDITOR_STEPS_LIST).exists(), 3, 30, "Step list not found");

        int found = $(Element.INTEGRATION_EDITOR_STEPS_LIST).shouldBe(visible)
            .$$(Element.INTEGRATION_EDITOR_STEPS_LIST_ITEM).size();
        assertEquals("Wrong number of steps", n, found);
    }

    @Then("^check there is a step with \"([^\"]*)\" title")
    public void checkStepTitle(String title) {
        assertThat(flowViewComponent.getStepsTitlesArray()).contains(title);
    }

    @Then("^check that (\\w+). step has ([^\"]*) title")
    public void checkParticularStepTitle(int positionOfStep, String title) {
        assertThat(flowViewComponent.getStepsTitlesArray().get(positionOfStep - 1)).contains(title);
    }

    @When("^edit integration step on position (\\d+)$")
    public void editIntegrationStep(int oneBasedStepPosition) {
        log.info("Editing integration step #" + oneBasedStepPosition);
        flowViewComponent.getStepOnPosition(oneBasedStepPosition)
            .$(Button.CONFIGURE).shouldBe(visible).click();
    }

    @When("^delete step on position (\\d+)$")
    public void deleteStepOnPosition(int oneBasedStepPosition) {
        log.info("Deleting integration step #" + oneBasedStepPosition);
        flowViewComponent.deleteStepOnPostion(oneBasedStepPosition);
    }

    @When("^publish integration$")
    public void publishIntegration() {
        log.info("Publishing integration");
        editor.getRootElement().$(Button.PUBLISH).shouldBe(visible).click();
    }

    @When("^save and cancel integration editor$")
    public void cancelIntegrationEditorSave() {
        editor.getRootElement().$(Button.SAVE_AS_DRAFT).shouldBe(visible).click();
        TestUtils.sleepIgnoreInterrupt(2000);
        editor.getRootElement().$(Button.CANCEL).shouldBe(visible).click();
        SelenideElement dialog = new ModalDialogPage().getRootElement();
        dialog.find(By.xpath(".//button[text()[contains(.,'Confirm')]]")).shouldBe(appears, Duration.ofSeconds(10)).click();
        dialog.shouldBe(Condition.not(visible), Duration.ofSeconds(10));
    }

    @Then("^validate that input datashape on step (\\d+) contains \"([^\"]*)\"$")
    public void validateInputDataShape(int stepPosition, String expectedDataShape) {
        assertThat(flowViewComponent.getStepOnPosition(stepPosition).$(Element.STEP_DATA_SHAPE).text())
            .isEqualToIgnoringCase(expectedDataShape);
    }

    @Then("^check that alert dialog contains text \"([^\"]*)\"$")
    public void checkAlertDialog(String expectedText) {
        ElementsCollection spans = editor.getDangerAlertElemet().findAll(By.tagName("span"));
        assertThat(spans.stream().anyMatch(span -> span.getText().contains(expectedText)))
            .isTrue()
            .as("In the alert dialog is not expected text.");
    }

    @Then("^check that alert dialog contains details \"([^\"]*)\"$")
    public void checkAlertDialogDetails(String expectedDetailsText) {
        SelenideElement alertElemet = editor.getDangerAlertElemet();
        alertElemet.find(By.tagName("button")).click();
        assertThat(alertElemet.find(By.tagName("pre")).getText()).isEqualTo(expectedDetailsText);
    }
}
