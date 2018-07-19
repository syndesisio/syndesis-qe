package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.xtf.jms.JmsClient;
import io.syndesis.qe.utils.JMSUtils;
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

    @Then("^verify that JMS message with content \"([^\"]*)\" was received from \"([^\"]*)\" \"([^\"]*)\"$")
    public void verifyThatJMSMessageWithContentWasReceivedFrom(String content, String type, String destination) {
        final String message = JMSUtils.getMessageText(JMSUtils.Destination.valueOf(type.toUpperCase()), destination);
        assertThat(message).isEqualTo(content);
    }

    @When("^publish message with content \"([^\"]*)\" to \"([^\"]*)\" with name \"([^\"]*)\"")
    public void publishMessageToDestinationWithName(String content, String type, String name) {
        JMSUtils.sendMessage(JMSUtils.Destination.valueOf(type.toUpperCase()), name, content);
    }
}
