package io.syndesis.qe.utils;

import javax.jms.Message;
import javax.jms.TextMessage;

import cz.xtf.jms.JmsClient;
import lombok.Setter;

public class JmsUtils {

    @Setter
    JmsClient jmsClient;

    public void sendMessage(String msgText, String type, String destinationFrom) throws Exception {
        this.setClientDestination(type, destinationFrom);
        jmsClient.sendMessage(msgText);
    }

    public String consumeMessage(String type, String destinationTo) throws Exception {
        this.setClientDestination(type, destinationTo);
        Message msg = jmsClient.receiveMessage();
        if (msg instanceof TextMessage) {
            return ((TextMessage) msg).getText();
        } else {
            throw new Exception("wrong type of message!!");
        }
    }

    //    AUXILIARIES:

    private void setClientDestination(String type, String destination) {
        if ("queue".equals(type)) {
            jmsClient = jmsClient.addQueue(destination);
        } else if ("topic".equals(type)) {
            jmsClient = jmsClient.addTopic(destination);
        } else {
            throw new IllegalArgumentException("must be queue or topic!!");
        }
    }
}
