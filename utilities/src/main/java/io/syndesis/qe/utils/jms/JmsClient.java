package io.syndesis.qe.utils.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import java.io.Serializable;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

/**
 * JMS Client class.
 *
 * @author David Simansky | dsimansk@redhat.com
 */
@Slf4j
public class JmsClient {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final long RECEIVE_TIMEOUT = 5000L;
    private ConnectionFactory factory;
    private Connection liveConnection;
    private Connection topicConnection;
    private String destinationName;
    private boolean isQueue;
    private boolean isPersistant = false;
    private boolean isTransacted = false;
    private boolean isDurable;
    private boolean keepAlive = false;
    private long timeToLive = 0;
    private int retries = 10;

    public JmsClient(ConnectionFactory factory) {
        this.factory = factory;
    }

    public JmsClient(Connection connection) {
        keepAlive = true;
        this.liveConnection = connection;
    }

    public JmsClient addQueue(String name) {
        if (destinationName != null) {
            throw new IllegalArgumentException("Can't set more than one destination per client");
        }
        this.destinationName = name;
        this.isQueue = true;
        return this;
    }

    public JmsClient addTopic(String name) {
        if (destinationName != null) {
            throw new IllegalArgumentException("Can't set more than one destination per client");
        }
        this.destinationName = name;
        this.isQueue = false;
        return this;
    }

    public JmsClient persistant() {
        this.isPersistant = true;
        return this;
    }

    public JmsClient transacted() {
        this.isTransacted = true;
        return this;
    }

    public JmsClient durable() {
        this.isDurable = true;
        return this;
    }

    public JmsClient setRetries(int setRetries) {
        this.retries = setRetries;
        return this;
    }

    public MessageConsumer createTopicConsumer() {
        return createTopicConsumer(null);
    }

    public MessageConsumer createTopicConsumer(String selector) {
        if (isQueue) {
            throw new IllegalArgumentException("Only for topic, not queue");
        }
        String consumerId = "consumer-" + UUID.randomUUID();
        topicConnection = startConnection(consumerId);
        Session session;
        try {
            session = topicConnection.createSession(isTransacted, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(destinationName);

            if (isDurable) {
                if (selector != null) {
                    return session.createDurableSubscriber(topic, consumerId, selector, true);
                } else {
                    return session.createDurableSubscriber(topic, consumerId);
                }
            } else {
                if (selector != null) {
                    return session.createConsumer(topic, selector);
                } else {
                    return session.createConsumer(topic);
                }
            }
        } catch (JMSException e) {
            log.error("Error creating topic consumer", e);
            e.printStackTrace();
        }
        return null;
    }

    public JmsClient keepAlive() {
        log.warn("When keepAlive is used, connection must be closed manually");
        this.keepAlive = true;
        return this;
    }

    public JmsClient timeToLive(long ttl) {
        this.timeToLive = ttl;
        return this;
    }

    public Message createMessage() {
        return createMessage(null);
    }

    public Message createMessage(Object messageObject) {
        Connection connection = null;
        Message result = null;
        Session session = null;
        try {
            connection = startConnection();
            session = connection.createSession(isTransacted, Session.AUTO_ACKNOWLEDGE);
            if (messageObject == null) {
                result = session.createMessage();
            } else {
                if (messageObject instanceof String) {
                    result = session.createTextMessage((String) messageObject);
                } else {
                    result = session.createObjectMessage((Serializable) messageObject);
                }
            }
        } catch (JMSException e) {
            log.error("Unable to create message", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("error with closing the session", e);
                    e.printStackTrace();
                } finally {
                    safeCloseConnection(connection);
                }
            }
        }
        return result;
    }

    public void sendMessage() {
        sendMessage("Hello, world!");
    }

    public void sendMessage(String messageText) {
        sendMessage(createMessage(messageText));
    }

    public void sendMessage(Message message) {
        Connection connection = null;
        Session session;
        try {
            connection = startConnection(); //try to be smarter here and initiate start connection

            session = connection.createSession(isTransacted, Session.AUTO_ACKNOWLEDGE);
            Destination dest;
            if (isQueue) {
                dest = session.createQueue(destinationName);
            } else {
                dest = session.createTopic(destinationName);
            }
            MessageProducer producer = session.createProducer(dest);

            if (isPersistant) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            }
            if (timeToLive > 0) {
                producer.setTimeToLive(timeToLive);
            }

            producer.send(message);

            producer.close();
            session.close();
        } catch (JMSException e) {
            log.error("unable to send message", e);
        } finally {
            safeCloseConnection(connection);
        }
    }

    public Message receiveMessage() {
        return receiveMessage(RECEIVE_TIMEOUT, null);
    }

    public Message receiveMessage(String selector) {
        return receiveMessage(RECEIVE_TIMEOUT, selector);
    }

    public Message receiveMessage(long timeout) {
        return receiveMessage(timeout, null);
    }

    public Message receiveMessage(long timeout, String selector) {
        Connection connection = null;
        Message result = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            connection = startConnection(); //try to be smarter here and start stable connection
            session = connection.createSession(isTransacted, Session.AUTO_ACKNOWLEDGE);
            Destination dest;

            if (isQueue) {
                dest = session.createQueue(destinationName);
            } else {
                dest = session.createTopic(destinationName);
            }

            if (selector != null) {
                consumer = session.createConsumer(dest, selector);
            } else {
                consumer = session.createConsumer(dest);
            }

            result = consumer.receive(timeout);
        } catch (JMSException e) {
            log.error("Unable to receive message", e);
            e.printStackTrace();
        } finally {
            try {
                if (consumer != null) {
                    consumer.close();
                }
                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                log.error("Problem with closing resources", e);
                e.printStackTrace();
            } finally {
                safeCloseConnection(connection);
            }
        }
        return result;
    }

    public static String getTextMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                return ((TextMessage) message).getText();
            } catch (JMSException e) {
                log.error("unable to get text message", e);
                e.printStackTrace();
            }
        }
        return null;
    }

    public void disconnect() {
        if (keepAlive && liveConnection != null) {
            safeCloseConnection(liveConnection);
        }
        if (topicConnection != null) {
            safeCloseConnection(topicConnection);
        }
    }

    public void close() {
        disconnect();
    }

    private Connection createConnection() throws JMSException {
        if (destinationName == null) {
            throw new IllegalArgumentException("Destination is null, can't send message to nowhere");
        }
        Connection connection;
        //if we don't have liveConnection, try to create fresh from factory
        if (keepAlive) {
            if (liveConnection == null) {
                liveConnection = factory.createConnection();
            }
            connection = liveConnection;
        } else {
            connection = factory.createConnection();
        }
        return connection;
    }

    private void safeCloseConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.stop();
                //only close if there isn't liveConnection
                if (!keepAlive) {
                    connection.close();
                }
            }
        } catch (JMSException e) {
            log.debug("Error while disconnecting", e);
        }
    }

    private Connection startConnection() {
        return startConnection(null);
    }

    private Connection startConnection(String consumerId) {
        Connection connection = null;
        int attempts = retries;
        while (connection == null && attempts > 0) {
            try {
                connection = createConnection();
                if ((!isQueue && consumerId != null) || keepAlive) {
                    connection.setExceptionListener(new ReconnectListener());
                }
                if (consumerId != null) {
                    connection.setClientID(consumerId);
                }
                Future<?> future = EXECUTOR.submit(new StartConnection(connection));
                future.get(15, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.warn("Interrupted while starting connection", ex);
            } catch (ExecutionException ex) {
                log.warn("Error during connection start, reattempt");
                log.debug("Exception: ", ex);
                connection = null;
                attempts--;
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    log.error("Failed to start connection, {} attempts remaining", attempts, e);
                }
            } catch (TimeoutException ex) {
                attempts--;
                safeCloseConnection(connection);
                connection = null;
                log.error("Failed to start connection, {} attempts remaining", attempts);
            } catch (JMSException ex) {
                if (ex.getCause() instanceof SocketException || ex.getMessage().contains("Connection reset")) {
                    log.warn("SocketException during connection start");
                    log.debug("Exception: ", ex);
                    connection = null;
                    attempts--;
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        log.error("Failed to start connection, {} attempts remaining", attempts, e);
                    }
                } else {
                    log.error("Failed to start connection", ex);
                }
            }
        }
        if (connection == null) {
            log.error("Unable to start connection, see logs for errors.");
        }
        return connection;
    }

    private static class StartConnection implements Callable<Void> {
        private final Connection connection;

        public StartConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() throws JMSException {
            connection.start();
            return null;
        }
    }

    /**
     * Simple ExceptionListener to help preserve connection
     */
    private class ReconnectListener implements ExceptionListener {
        private int retries = 3;

        @Override
        public void onException(JMSException e) {
            log.debug("ExceptionListener invoked");
            try {
                if (retries > 0) {
                    log.debug("Attempting to reconnect, retries left {}", retries);
                    retries--;
                    if (topicConnection != null && !isQueue) {
                        topicConnection.start();
                    } else if (liveConnection != null) {
                        liveConnection.start();
                    }
                } else {
                    log.debug("Unable to reconnect", e);
                }
            } catch (JMSException ex) {
                log.debug("Exception thrown in ExceptionListener reconnect", ex);
            }
        }
    }
}
