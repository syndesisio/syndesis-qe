package io.syndesis.qe.utils.jms;

import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.JMSException;
import javax.jms.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JMSUtils {
    public enum Destination {
        QUEUE, TOPIC
    }

    private static final String JMS_APP_NAME = "syndesis-amq";
    private static final String JMS_USER = "amq";
    private static final String JMS_PASS = "topSecret";
    private static final String PROTOCOL = "tcp";

    public static void sendMessage(String appName, String jmsProtocol, String user, String pass, Destination type, String destName, String message) {
        JmsClientManager.sendMessage(appName, jmsProtocol, user, pass, client -> withDestination(client, type, destName).sendMessage(message));
    }

    public static void sendMessage(String jmsProtocol, Destination type, String name, String content) {
        sendMessage(JMS_APP_NAME, jmsProtocol, JMS_USER, JMS_PASS, type, name, content);
    }

    public static void sendMessage(Destination type, String name, String content) {
        sendMessage(JMS_APP_NAME, PROTOCOL, JMS_USER, JMS_PASS, type, name, content);
    }

    public static Message getMessage(String appName, String jmsProtocol, String user, String pass, Destination type, String destinationName,
        long timeout) {
        return JmsClientManager.receiveMessage(appName, jmsProtocol, user, pass, client ->
            withDestination(client, type, destinationName)
                .receiveMessage(timeout));
    }

    public static Message getMessage(Destination type, String destinationName) {
        return getMessage(PROTOCOL, type, destinationName, 60000L);
    }

    public static Message getMessage(Destination type, String destinationName, long timeout) {
        return getMessage(PROTOCOL, type, destinationName, timeout);
    }

    public static Message getMessage(String jmsProtocol, Destination type, String destinationName, long timeout) {
        return getMessage(JMS_APP_NAME, jmsProtocol, JMS_USER, JMS_PASS, type, destinationName, timeout);
    }

    public static String getMessageText(Destination type, String destinationName) {
        return getMessageText(JMS_APP_NAME, PROTOCOL, JMS_USER, JMS_PASS, type, destinationName);
    }

    public static String getMessageText(String jmsProtocol, Destination type, String destinationName) {
        return getMessageText(JMS_APP_NAME, jmsProtocol, JMS_USER, JMS_PASS, type, destinationName);
    }

    public static String getMessageText(String appName, String jmsProtocol, String user, String pass, Destination type, String destinationName) {
        Message m = getMessage(appName, jmsProtocol, user, pass, type, destinationName, 60000L);
        if (m == null) {
            return null;
        }
        String text = null;
        try {
            if (m instanceof JmsTextMessage) {
                text = ((JmsTextMessage) m).getText();
            } else if (m instanceof ActiveMQBytesMessage) {
                text = new String(((ActiveMQBytesMessage) m).getContent().getData());
            } else {
                text = ((ActiveMQTextMessage) m).getText();
            }
        } catch (JMSException e) {
            log.error("Unable to get text from message", e);
            e.printStackTrace();
        }

        log.debug("Got message: " + text);
        return text;
    }

    public static void clear(Destination type, String name) {
        Message m = getMessage(PROTOCOL, type, name, 5000L);
        while (m != null) {
            m = getMessage(PROTOCOL, type, name, 5000L);
        }
    }

    private static JmsClient withDestination(JmsClient c, Destination type, String name) {
        if (Destination.QUEUE == type) {
            c.addQueue(name);
        } else {
            c.addTopic(name);
        }
        return c;
    }
}
