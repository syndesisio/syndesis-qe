package io.syndesis.qe.utils;

import org.apache.activemq.ActiveMQConnectionFactory;

import cz.xtf.jms.JmsClient;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmsClientManager implements AutoCloseable {

    private String jmsUrl;
    private int jmsPort;
    private String jmsPodName = "broker-amq";
    private String jmsUser = "amq";
    private String jmsPass = "topSecret";
    private LocalPortForward jmsLocalPortForward = null;
    private JmsClient jmsClient;

    public JmsClientManager(String protocol) {
        initValues(protocol);
    }

    private void initValues(String protocol) {
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

    public JmsClient getClient() {

        if (jmsLocalPortForward == null || !jmsLocalPortForward.isAlive()) {
            //can be the same pod twice forwarded to different ports? YES
            Pod  pod = OpenShiftUtils.xtf().getAnyPod("app", jmsPodName);
            log.info("POD NAME: *{}*", pod.getMetadata().getName());
            jmsLocalPortForward = OpenShiftUtils.portForward(pod, jmsPort, jmsPort);
        }
        return this.initClient();
    }

    public void close() {
        if (jmsClient != null) {
            jmsClient.disconnect();
        }
        TestUtils.terminateLocalPortForward(jmsLocalPortForward);
    }

    private JmsClient initClient() {
        if (jmsClient == null) {
            ActiveMQConnectionFactory jmsFactory = new ActiveMQConnectionFactory();
            jmsFactory.setBrokerURL(jmsUrl);
            jmsFactory.setUserName(jmsUser);
            jmsFactory.setPassword(jmsPass);
            jmsClient = new JmsClient(jmsFactory);
        }
        return jmsClient;
    }
}
