package io.syndesis.qe.bdd.validation;

import cz.xtf.jms.JmsClient;
import org.assertj.core.api.Assertions;

import cucumber.api.java.en.Then;
import io.syndesis.qe.utils.JmsClientManager;

public class JmsValidationSteps {

    private final String messageText = "ZIL SOM NAPLNO";

    public JmsValidationSteps() {
    }

    @Then("^verify that JMS message using \"([^\"]*)\" protocol, published on \"([^\"]*)\" named \"([^\"]*)\" has arrived to \"([^\"]*)\" named \"([^\"]*)\" consumer$")
    public void verifyJMSconnection(String protocol, String typeFrom, String destinationFrom, String typeTo, String destinationTo) {
        try(JmsClientManager manager = new JmsClientManager(protocol)) {
            JmsClient jmsClient = manager.getClient();
            addDestination(jmsClient, destinationFrom, typeFrom);
            jmsClient.sendMessage(messageText);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try(JmsClientManager manager = new JmsClientManager(protocol)) {
            JmsClient jmsClient = manager.getClient();
            addDestination(jmsClient, destinationTo, typeTo);
            String textMessage = JmsClient.getTextMessage(jmsClient.receiveMessage());
            Assertions.assertThat(textMessage).isEqualTo(messageText);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }
    }

    private void addDestination(JmsClient jmsClient, String destination, String typeFrom) {
        if ("queue".equals(typeFrom)) {
            jmsClient.addQueue(destination);
        } else {
            jmsClient.addTopic(destination);
        }
    }

}
