package io.syndesis.qe.steps.other;

import static org.assertj.core.api.Assertions.assertThat;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.steps.CommonSteps;
import io.syndesis.qe.steps.connections.wizard.phases.NameConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.SelectConnectionTypeSteps;
import io.syndesis.qe.utils.ByUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.codeborne.selenide.ElementsCollection;

import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
    @When("^created Kafka connection using \"([^\"]*)\" security with name \"([^\"]*)\"( with SASL mechanism (PLAIN|OAUTHBEARER))?$")
    public void createdKafkaConnectionUsingAMQStreamsAutoDetection(String securityMode, String connectionName, String saslMechanism) {
            String connectionType = "Kafka Message Broker";
        String description = "Kafka Streams Auto Detection";

        commonSteps.navigateTo("Connections");
        commonSteps.validatePage("Connections");
        ElementsCollection connections = connectionsPage.getAllConnections();
        connections = connections.filter(exactText(connectionName));
        assertThat(connections.isEmpty()).isTrue();
        commonSteps.clickOnLink("Create Connection");
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay() * 1000);
        selectConnectionTypeSteps.selectConnectionType(connectionType);

        Account kafkaAccount = null;
        if ("SASL_SSL".equals(securityMode)) {
            // Managed kafka
            kafkaAccount = AccountsDirectory.getInstance().get(Account.Name.MANAGED_KAFKA);
            connectionsPage.fillInput($(ByUtils.containsId("-select-typeahead")), TestConfiguration.managedKafkaBootstrapUrl());
            commonSteps.clickOnButton("Create \"" + TestConfiguration.managedKafkaBootstrapUrl() + "\"");
            connectionsPage.fillInput($(ByUtils.containsId("username")), kafkaAccount.getProperty("clientID"));
            connectionsPage.fillInput($(ByUtils.containsId("password")), kafkaAccount.getProperty("clientSecret"));
            if (saslMechanism.contains("OAUTHBEARER")) {
                commonSteps.selectsFromDropdown("OAUTHBEARER", "saslmechanism");
            } else {
                commonSteps.selectsFromDropdown("PLAIN", "saslmechanism");
                connectionsPage.fillInput($(ByUtils.containsId("sasllogincallbackhandlerclass")), "");
                connectionsPage.fillInput($(ByUtils.containsId("oauthtokenendpointuri")), "");
            }
        } else {
            // Kafka on cluster
            kafkaAccount = AccountsDirectory.getInstance().get("kafka-autodetect-" + securityMode.toLowerCase());
            if (OpenShiftUtils.isOpenshift3()) {
                // test uses older AMQ Streams on 3.11 so autodiscover function is not working
                connectionsPage.fillInput($(ByUtils.containsId("-select-typeahead")), kafkaAccount.getProperty("brokers"));
                commonSteps.clickOnButton("Create \"" + kafkaAccount.getProperty("brokers") + "\"");
            } else {
                //select autodiscovered broker url:
                commonSteps.clickOnButtonByCssClassName("pf-c-select__toggle-button");
                commonSteps.clickOnButton(kafkaAccount.getProperty("brokers"));
            }
            if ("TLS".equals(securityMode)) {
                $(ByUtils.dataTestId("brokercertificate")).shouldBe(visible).sendKeys(kafkaAccount.getProperty("brokercertificate"));
            }
        }
        commonSteps.selectsFromDropdown(kafkaAccount.getProperty("transportprotocol"), "transportprotocol");
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
        Account kafkaTlsAccount = AccountsDirectory.getInstance().get("kafka-autodetect-tls");
        Map<String, String> kafkaAutodetectTlsParameters = kafkaTlsAccount.getProperties();
        kafkaAutodetectTlsParameters.put("brokercertificate", cert);
        kafkaTlsAccount.setProperties(kafkaAutodetectTlsParameters);
    }

    private String extractValue(String word, String input) {
        final String pattern = String.format("%s=(.*?),", word);
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (m.group(1).contains("{}")) {
                // since OCP 4.5, yaml contains metadata.managedFields so crt is there twice and one is empty. Ignore empty one.
                continue;
            }
            sb.append(m.group(1));
        }
        return decode(sb.toString());
    }

    private String decode(String encrypted) {
        byte[] result = Base64.getDecoder().decode(encrypted);
        return new String(result);
    }
}
