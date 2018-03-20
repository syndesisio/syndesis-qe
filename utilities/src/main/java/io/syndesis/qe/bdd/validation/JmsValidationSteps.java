package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import cucumber.api.java.en.Then;
import io.syndesis.qe.utils.JmsClientManager;
import io.syndesis.qe.utils.JmsUtils;

public class JmsValidationSteps {

    private final JmsUtils jmsUtils = new JmsUtils();
    private final String messageText = "ZIL SOM NAPLNO";

    public JmsValidationSteps() {
    }

    @Then("^verify that JMS message using \"([^\"]*)\" protocol, published on \"([^\"]*)\" named \"([^\"]*)\" has arrived to \"([^\"]*)\" named \"([^\"]*)\" consumer$")
    public void verifyJMSconnection(String protocol, String typeFrom, String destinationFrom, String typeTo, String destinationTo) {
        final JmsClientManager clientManager = new JmsClientManager(protocol);
        jmsUtils.setJmsClient(clientManager.getClient());

        try {
            jmsUtils.sendMessage(messageText, typeFrom, destinationFrom);
            String returnMessageText = jmsUtils.consumeMessage(typeTo, destinationTo);
            Assertions.assertThat(returnMessageText).isEqualTo(messageText);

        } catch (Exception e) {
            Assertions.fail("Error: " + e);
        } finally {
            clientManager.closeJmsClient();
        }
    }
}
