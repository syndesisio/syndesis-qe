package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.util.servicenow.ServiceNow;
import io.syndesis.qe.util.servicenow.model.Incident;
import io.syndesis.qe.utils.ServiceNowUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.jms.JMSUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ServiceNowValidationSteps {
    private static String incidentId;
    @Autowired
    private ServiceNowUtils snUtils;

    @When("create incident with {string} number")
    public void createIncidentWithNumber(String number) {
        Incident i = Incident.getSampleIncident();
        i.setNumber(snUtils.modifySNNumber(number));
        i.setDescription("Automated create incident test-updated");
        i.setSeverity(BigInteger.valueOf(2));
        incidentId = ServiceNow.createIncident(i).getSysId();
    }

    @When("verify that received incident from {string} queue contains {string}")
    public void verifyThatReceivedIncidentFromQueueContains(String queue, String contains) {
        String message = JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queue);
        ServiceNow.deleteIncident(incidentId);
        assertThat(message).contains(snUtils.modifySNNumber(contains));
        assertThat(message).contains(incidentId);
    }

    @When("send {string} incident to {string} queue and verify it was created in SN")
    public void sendIncidentToQueueAndVerifyItWasCreatedInSN(String incidentNumber, String queue) {
        incidentNumber = snUtils.modifySNNumber(incidentNumber);
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

    @Then("verify that incident with {string} number has {string} description")
    public void verifyIncidentsWithNumber(String numbers, String description) {
        List<Incident> list = ServiceNow.getFilteredIncidents("number=" + snUtils.modifySNNumber(numbers), 1);
        Assertions.assertThat(list).isNotEmpty();

        Incident created = list.get(0);
        Assertions.assertThat(created.getDescription()).contains(description);
    }

    @When("delete incidents with {string} number")
    public void deleteIncidentsWithNumber(String numbers) {
        final String[] incidentNumbers = numbers.split(",");
        List<Incident> incidents = new ArrayList<>();

        Arrays.asList(incidentNumbers)
            .forEach(number -> incidents.addAll(ServiceNow.getFilteredIncidents("number=" + snUtils.modifySNNumber(number), 100)));
        incidents.forEach(incident -> ServiceNow.deleteIncident(incident.getSysId()));
    }

}
