package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.util.servicenow.ServiceNow;
import io.syndesis.qe.util.servicenow.model.Incident;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.jms.JMSUtils;

import org.assertj.core.api.Assertions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ServiceNowValidationSteps {
    private static String incidentId;
    private static final String BROWSER = TestConfiguration.syndesisBrowser();

    @When("^create incident with \"([^\"]*)\" number$")
    public void createIncidentWithNumber(String number) {
        Incident i = Incident.getSampleIncident();
        i.setNumber(modifySNNumber(number));
        i.setDescription("Automated create incident test-updated");
        i.setSeverity(BigInteger.valueOf(2));
        incidentId = ServiceNow.createIncident(i).getSysId();
    }

    @When("^verify that received incident from \"([^\"]*)\" queue contains \"([^\"]*)\"$")
    public void verifyThatReceivedIncidentFromQueueContains(String queue, String contains) {
        String message = JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queue);
        ServiceNow.deleteIncident(incidentId);
        assertThat(message).contains(modifySNNumber(contains));
        assertThat(message).contains(incidentId);
    }

    @When("^send \"([^\"]*)\" incident to \"([^\"]*)\" queue and verify it was created in SN$")
    public void sendIncidentToQueueAndVerifyItWasCreatedInSN(String incidentNumber, String queue) {
        incidentNumber = modifySNNumber(incidentNumber);
        final String description = "CRTDINC01" + UUID.randomUUID().toString().substring(0, 8);
        Incident i = Incident.getSampleIncident();
        i.setNumber(incidentNumber);
        i.setDescription(description);
        JMSUtils.sendMessage(JMSUtils.Destination.valueOf("QUEUE"), queue, ServiceNow.getIncidentJson(i));
        //wait till message gets through integration
        TestUtils.sleepForJenkinsDelayIfHigher(5);
        List<Incident> list = ServiceNow.getFilteredIncidents("number=" + incidentNumber, 1);
        Assertions.assertThat(list).isNotEmpty();

        Incident created = list.get(0);
        ServiceNow.deleteIncident(created.getSysId());
        Assertions.assertThat(created.getNumber()).isEqualToIgnoringCase(i.getNumber());
        Assertions.assertThat(created.getDescription()).contains(description);
    }

    @Then("^verify that incident with \"([^\"]*)\" number has \"([^\"]*)\" description")
    public void verifyIncidentsWithNumber(String numbers, String description) {
        List<Incident> list = ServiceNow.getFilteredIncidents("number=" + numbers, 1);
        Assertions.assertThat(list).isNotEmpty();

        Incident created = list.get(0);
        Assertions.assertThat(created.getDescription()).contains(description);
    }

    @When("^delete incidents with \"([^\"]*)\" number$")
    public void deleteIncidentsWithNumber(String numbers) {
        final String[] incidentNumbers = numbers.split(",");
        List<Incident> incidents = new ArrayList<>();

        Arrays.asList(incidentNumbers).forEach(number -> incidents.addAll(ServiceNow.getFilteredIncidents("number=" + modifySNNumber(number), 100)));
        incidents.forEach(incident -> ServiceNow.deleteIncident(incident.getSysId()));
    }

    public static String modifySNNumber(String input) {
        System.out.println(BROWSER);
        if (input.contains("{number1}")) {
            return input.replace("{number1}", "QA" + BROWSER.substring(0, 4).toUpperCase() + "1");
        } else if (input.contains("{number2}")) {
            return input.replace("{number2}", "QACR" + BROWSER.substring(0, 2).toUpperCase() + "1");
        } else {
            // add browser info to the other input
            return input + BROWSER.substring(0, 2).toUpperCase() + "1";
        }
    }
}
