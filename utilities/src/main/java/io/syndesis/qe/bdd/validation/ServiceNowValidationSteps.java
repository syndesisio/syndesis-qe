package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cucumber.api.java.en.When;
import io.syndesis.qe.servicenow.ServiceNow;
import io.syndesis.qe.servicenow.model.Incident;
import io.syndesis.qe.utils.JMSUtils;
import io.syndesis.qe.utils.TestUtils;

public class ServiceNowValidationSteps {
    private static String incidentId;

    @When("^create incident with \"([^\"]*)\" number$")
    public void createIncidentWithNumber(String number) {
        Incident i = Incident.getSampleIncident();
        i.setNumber(number);
        i.setDescription("Automated create incident test-updated");
        i.setSeverity(BigInteger.valueOf(2));
        incidentId = ServiceNow.createIncident(i).getSysId();
    }

    @When("^verify that received incident from \"([^\"]*)\" queue contains \"([^\"]*)\"$")
    public void verifyThatReceivedIncidentFromQueueContains(String queue, String contains) {
        String message = JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queue);
        ServiceNow.deleteIncident(incidentId);
        assertThat(message).contains(contains);
        assertThat(message).contains(incidentId);
    }

    @When("^send \"([^\"]*)\" incident to \"([^\"]*)\" queue and verify it was created in SN$")
    public void sendIncidentToQueueAndVerifyItWasCreatedInSN(String incidentNumber, String queue) {
        final String description = "CRTDINC01" + UUID.randomUUID().toString().substring(0, 8);
        Incident i = Incident.getSampleIncident();
        i.setNumber(incidentNumber);
        i.setDescription(description);
        JMSUtils.sendMessage(JMSUtils.Destination.valueOf("QUEUE"), queue, ServiceNow.getIncidentJson(i));
        //wait till message gets through integration
        TestUtils.sleepForJenkinsDelayIfHigher(3);
        Incident created = ServiceNow.getFilteredIncidents("number=" + incidentNumber, 1).get(0);
        ServiceNow.deleteIncident(created.getSysId());
        assertThat(created.getNumber()).isEqualToIgnoringCase(i.getNumber());
        assertThat(created.getDescription()).contains(description);
    }

    @When("^delete incidents with \"([^\"]*)\" number$")
    public void deleteIncidentsWithNumber(String numbers) {
        final String[] incidentNumbers = numbers.split(",");
        List<Incident> incidents = new ArrayList<>();

        Arrays.asList(incidentNumbers).forEach(number -> incidents.addAll(ServiceNow.getFilteredIncidents("number=" + number, 100)));
        incidents.forEach(incident -> ServiceNow.deleteIncident(incident.getSysId()));
    }
}
