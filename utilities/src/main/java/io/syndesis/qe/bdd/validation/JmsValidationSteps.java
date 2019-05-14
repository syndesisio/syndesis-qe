package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.utils.JMSUtils;
import io.syndesis.qe.utils.JmsClientManager;

import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.xtf.jms.JmsClient;

public class JmsValidationSteps {

    private final String messageText = "ZIL SOM NAPLNO";

    public JmsValidationSteps() {
    }

    @Then("^verify that JMS message using \"([^\"]*)\" protocol, published on \"([^\"]*)\" named \"([^\"]*)\" has arrived to \"([^\"]*)\" named \"" +
        "([^\"]*)\" consumer$")
    public void verifyJMSconnection(String protocol, String typeFrom, String destinationFrom, String typeTo, String destinationTo) {
        try (JmsClientManager manager = new JmsClientManager(protocol)) {
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

        try (JmsClientManager manager = new JmsClientManager(protocol)) {
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

    @Then("^verify that JMS message with content \'([^\']*)\' was received from \"([^\"]*)\" \"([^\"]*)\"$")
    public void verifyThatJMSMessageWithContentWasReceivedFrom(String content, String type, String destination) {
        final String message = JMSUtils.getMessageText(JMSUtils.Destination.valueOf(type.toUpperCase()), destination);
        assertThat(message).isEqualTo(content);
    }

    @When("^publish message with content \"([^\"]*)\" to \"([^\"]*)\" with name \"([^\"]*)\"")
    public void publishMessageToDestinationWithName(String content, String type, String name) {
        JMSUtils.sendMessage(JMSUtils.Destination.valueOf(type.toUpperCase()), name, content);
    }

    @Given("^clean destination type \"([^\"]*)\" with name \"([^\"]*)\"")
    public void cleanDestination(String type, String name) {
        JMSUtils.clear(JMSUtils.Destination.valueOf(type.toUpperCase()), name);
    }

    /**
     * Load JMS message from resource and send it to the topic/queue with name
     *
     * @param resourceName - name of resource file with the message
     * @param type - queue or topic
     * @param name - name of topic/queue
     */
    @When("^publish JMS message from resource \"([^\"]*)\" to \"([^\"]*)\" with name \"([^\"]*)\"")
    public void publishMessageFromResourceToDestinationWithName(String resourceName, String type, String name) throws IOException {

        ClassLoader classLoader = this.getClass().getClassLoader();
        URL fileUrl = classLoader.getResource("jms_messages/" + resourceName);
        if (fileUrl == null) {
            fail("File with name " + resourceName + " doesn't exist in the resources");
        }

        File file = new File(fileUrl.getFile());
        String jmsMessage = new String(Files.readAllBytes(file.toPath()));
        JMSUtils.sendMessage(JMSUtils.Destination.valueOf(type.toUpperCase()), name, jmsMessage);
    }

    @Then("^verify that the JMS queue \"([^\"]*)\" is empty$")
    public void verifyEmptyQueue(String queue) {
        // This waits up to 60 seconds to get something for the queue
        assertThat(JMSUtils.getMessage(JMSUtils.Destination.QUEUE, queue)).isNull();
    }

    @Then("^verify that (\\d+) messages? (?:were|was) received from JMS queue \"([^\"]*)\"$")
    public void verifyEmptyQueue(int count, String queue) {
        for (int i = 0; i < count; i++) {
            assertThat(JMSUtils.getMessage(JMSUtils.Destination.QUEUE, queue)).isNotNull();
        }
        verifyEmptyQueue(queue);
    }

    @Then("^verify that JMS queue \"([^\"]*)\" received a message in (\\d+) seconds$")
    public void verifyEmptyQueue(String queue, int secondsTimeout) {
        assertThat(JMSUtils.getMessage(JMSUtils.Destination.QUEUE, queue, secondsTimeout * 1000L)).isNotNull();
    }
}
