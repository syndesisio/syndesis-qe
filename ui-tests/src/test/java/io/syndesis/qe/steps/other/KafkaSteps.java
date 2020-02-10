package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exactText;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.connections.wizard.phases.NameConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.SelectConnectionTypeSteps;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.ElementsCollection;

import java.util.List;

import cucumber.api.java.en.Given;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaSteps {
    private final NameConnectionSteps nameConnectionSteps = new NameConnectionSteps();
    private final CommonSteps commonSteps = new CommonSteps();
    private final Connections connectionsPage = new Connections();
    @Autowired
    private SelectConnectionTypeSteps selectConnectionTypeSteps;

    // this method is preliminary, since auto-detection dropdown doesn't have data-testid yet and
    // thus does not fit ...fillConnectionDetails(...) method of CommonSteps.createConnections()
    @Given("created Kafka connection using AMQ streams auto detection")
    public void createdKafkaConnectionUsingAMQStreamsAutoDetection(DataTable connectionsData) {
        List<List<String>> dataTable = connectionsData.cells();

        List<String> dataRow = dataTable.get(0);
        String connectionType = dataRow.get(0);
        String connectionName = dataRow.get(2);
        String connectionDescription = dataRow.get(3);

        commonSteps.navigateTo("Connections");
        commonSteps.validatePage("Connections");

        ElementsCollection connections = connectionsPage.getAllConnections();
        connections = connections.filter(exactText(connectionName));
        assertThat(connections.isEmpty()).isTrue();

        commonSteps.clickOnLink("Create Connection");
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay() * 1000);
        selectConnectionTypeSteps.selectConnectionType(connectionType);

        commonSteps.clickOnButtonByCssClassName("pf-c-select__toggle-button");
        commonSteps.clickOnButtonByCssClassName("pf-c-select__menu-item");
        commonSteps.clickOnButton("Validate");
        commonSteps.successNotificationIsPresentWithError(connectionType + " has been successfully validated");
        commonSteps.scrollTo("top", "right");
        commonSteps.clickOnButton("Next");

        //next page:
        nameConnectionSteps.setConnectionName(connectionName);
        nameConnectionSteps.setConnectionDescription(connectionDescription);
        commonSteps.clickOnButton("Save");
    }
}
