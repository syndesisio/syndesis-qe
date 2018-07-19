package io.syndesis.qe.utils;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.Message;

import cz.xtf.jms.JmsClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JMSUtils {
    public enum Destination {
        QUEUE, TOPIC
    }

    public static Message getMessage(Destination type, String destinationName) {
        Message m;
        try(JmsClientManager manager = new JmsClientManager("tcp")) {
            JmsClient jmsClient = manager.getClient();
            if (Destination.QUEUE.equals(type)) {
                m = jmsClient.addQueue(destinationName).receiveMessage(60000L);
            } else {
                m = jmsClient.addTopic(destinationName).receiveMessage(60000L);
            }
            return m;
        } catch (Exception e) {
            log.error("Unable to get message from JMS", e);
            e.printStackTrace();
        }
        return null;
    }

    public static String getMessageText(Destination type, String destionationName) {
        Message m = getMessage(type, destionationName);
        if (m == null) {
            return null;
        }
        String text = null;
        if (m instanceof ActiveMQBytesMessage) {
            text = new String(((ActiveMQBytesMessage)m).getContent().getData());
        } else {
            try {
                text = ((ActiveMQTextMessage) m).getText();
            } catch (JMSException e) {
                log.error("Unable to get text from message", e);
                e.printStackTrace();
            }
        }
        log.debug("Got message: " + text);
        return text;
    }

    public static void sendMessage(Destination type, String name, String content) {
        try(JmsClientManager manager = new JmsClientManager("tcp")) {
            JmsClient jmsClient = manager.getClient();
            if (type.equals(Destination.QUEUE)) {
                jmsClient.addQueue(name);
            } else {
                jmsClient.addTopic(name);
            }
            jmsClient.sendMessage(content);
        } catch (Exception e) {
            log.error("Unable to send message to queue", e);
            e.printStackTrace();
        }
    }
}
