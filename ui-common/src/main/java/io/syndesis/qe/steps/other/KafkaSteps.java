package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.connections.wizard.phases.NameConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.SelectConnectionTypeSteps;
import io.syndesis.qe.utils.AccountUtils;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.ElementsCollection;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.api.model.Secret;
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
    @Given("created Kafka connection using AMQ streams auto detection using \"([^\"]*)\" security$")
    public void createdKafkaConnectionUsingAMQStreamsAutoDetection(String securityMode, DataTable connectionsData) {

        final List<List<String>> dataTable = connectionsData.cells();

        final List<String> dataRow = dataTable.get(0);
        String connectionType = dataRow.get(0);
        String connectionName = dataRow.get(2);
        String description = dataRow.get(3);

        Account kafkaAccount = AccountUtils.get("kafka-autodetect-" + securityMode.toLowerCase());

        commonSteps.navigateTo("Connections");
        commonSteps.validatePage("Connections");

        ElementsCollection connections = connectionsPage.getAllConnections();
        connections = connections.filter(exactText(connectionName));
        assertThat(connections.isEmpty()).isTrue();

        commonSteps.clickOnLink("Create Connection");
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay() * 1000);
        selectConnectionTypeSteps.selectConnectionType(connectionType);

        //select autodiscovered broker url:
        commonSteps.clickOnButtonByCssClassName("pf-c-select__toggle-button");
        commonSteps.clickOnButton(kafkaAccount.getProperty("brokers"));
        commonSteps.selectsFromDropdown(kafkaAccount.getProperty("transportprotocol"), "transportprotocol");
        if ("TLS".equals(securityMode)) {
            $(ByUtils.dataTestId("brokercertificate")).shouldBe(visible).sendKeys(kafkaAccount.getProperty("brokercertificate"));
        }
        commonSteps.clickOnButton("Validate");

        commonSteps.successNotificationIsPresentWithError(connectionType + " has been successfully validated", "success");
        commonSteps.scrollTo("top", "right");
        commonSteps.clickOnButton("Next");

        //next page:
        nameConnectionSteps.setConnectionName(connectionName);
        nameConnectionSteps.setConnectionDescription(description);

        commonSteps.clickOnButton("Save");
    }

    @Then("extract broker certificate")
    public void extractBrokerCertificate() {
        //        1. get string with values: (oc get secret my-cluster-kafka-brokers -o yaml > sec.log)
        Secret secret = OpenShiftUtils.getInstance().getSecret("my-cluster-kafka-brokers");
        String secrets = secret.toString();

        //        2. extract values:
        String cert = extractValue("crt", secrets);

        //        3. put values into account:
        Account kafkaTlsAccount = AccountUtils.get("kafka-autodetect-tls");
        Map<String, String> kafkaAutodetectTlsParameters = kafkaTlsAccount.getProperties();
        kafkaAutodetectTlsParameters.put("brokercertificate", cert);
        kafkaTlsAccount.setProperties(kafkaAutodetectTlsParameters);
    }

    private String extractValue(String word, String input) {
        final String pattern = String.format(".*%s=.*?,", word);
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group(0).replaceAll(String.format(".*%s=", word), "").replaceAll("[,}]", ""));
        }
        return decode(sb.toString());
    }

    private String decode(String encrypted) {
        byte[] result = Base64.getDecoder().decode(encrypted);
        return new String(result);
    }
}
