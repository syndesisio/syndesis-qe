package io.syndesis.qe.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.Message;

import java.util.function.Consumer;
import java.util.function.Function;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmsClientManager {
    private String jmsUrl;
    private int jmsPort;
    private String jmsAppName;
    private String jmsUser;
    private String jmsPass;
    private String protocol;
    private LocalPortForward jmsLocalPortForward = null;
    private JmsClient jmsClient;

    public JmsClientManager(String protocol) {
        initValues(protocol);
    }

    public JmsClientManager(String jmsAppName, String protocol, String jmsUser, String jmsPass) {
        this.jmsAppName = jmsAppName;
        this.jmsUser = jmsUser;
        this.jmsPass = jmsPass;
        initValues(protocol);
    }

    private void initValues(String setProtocol) {
        this.protocol = setProtocol;
        switch (protocol) {
            case "tcp":
            case "openwire":
                jmsPort = 61616;
                jmsUrl = "tcp://127.0.0.1:" + jmsPort;
                break;
            case "amqp":
                jmsPort = 5672;
                jmsUrl = "amqp://127.0.0.1:" + jmsPort;
                break;
        }
    }

    public static void sendMessage(String jmsAppName, String protocol, String jmsUser, String jmsPass, Consumer<JmsClient> client) {
        JmsClientManager manager = new JmsClientManager(jmsAppName, protocol, jmsUser, jmsPass);

        try {
            JmsClient jmsClient = manager.getClient();
            client.accept(jmsClient);
        } finally {
            manager.close();
        }
    }

    public static Message receiveMessage(String jmsAppName, String protocol, String jmsUser, String jmsPass, Function<JmsClient, Message> block) {
        JmsClientManager manager = new JmsClientManager(jmsAppName, protocol, jmsUser, jmsPass);

        try {
            JmsClient jmsClient = manager.getClient();
            return block.apply(jmsClient);
        } finally {
            manager.close();
        }
    }

    private JmsClient getClient() {
        if (jmsLocalPortForward == null || !jmsLocalPortForward.isAlive()) {
            //can be the same pod twice forwarded to different ports? YES
            Pod pod = OpenShiftUtils.xtf().getAnyPod("app", jmsAppName);
            jmsLocalPortForward = OpenShiftUtils.portForward(pod, jmsPort, jmsPort);
        }
        return this.initClient();
    }

    private void close() {
        if (jmsClient != null) {
            jmsClient.disconnect();
            jmsClient = null;
        }
        TestUtils.terminateLocalPortForward(jmsLocalPortForward);
    }

    private JmsClient initClient() {
        if (jmsClient == null) {
            switch (protocol) {
                case "tcp":
                case "openwire":
                    ActiveMQConnectionFactory jmsFactory = new ActiveMQConnectionFactory();
                    jmsFactory.setBrokerURL(jmsUrl);
                    jmsFactory.setUserName(jmsUser);
                    jmsFactory.setPassword(jmsPass);
                    jmsClient = new JmsClient(jmsFactory);
                    break;
                case "amqp":
                    JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(jmsUser, jmsPass, jmsUrl);
                    jmsClient = new JmsClient(jmsConnectionFactory);
                    break;
            }
        }
        return jmsClient;
    }
}
