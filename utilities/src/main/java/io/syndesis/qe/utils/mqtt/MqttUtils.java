package io.syndesis.qe.utils.mqtt;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Optional;


@Slf4j
public class MqttUtils {
    private static final String broker = "tcp://localhost:1883";


    public MqttClient createReceiver(String clientName, String topic) throws MqttException {
        MqttClient sampleClient = new MqttClient(broker, clientName, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("QE MQTT");
        if (optional.isPresent()) {
            log.info("Setting username and password for QE MQTT client");
            connOpts.setUserName(optional.get().getProperties().get("userName"));
            connOpts.setPassword(optional.get().getProperties().get("password").toCharArray());
        }

        sampleClient.connect(connOpts);
        sampleClient.subscribe(topic, 1);
        sampleClient.setCallback(new Receiver(clientName));
        return sampleClient;
    }

    public void sendMessage(String messageContent, String topic) {
        final String clientId = "syndesis-mqtt-sender";
        MqttClient sampleClient = null;
        try {
            sampleClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);

            Optional<Account> optional = AccountsDirectory.getInstance().getAccount("QE MQTT");
            if (optional.isPresent()) {
                log.info("Setting username and password for QE MQTT client");
                connOpts.setUserName(optional.get().getProperties().get("userName"));
                connOpts.setPassword(optional.get().getProperties().get("password").toCharArray());
            }

            sampleClient.connect(connOpts);
            MqttMessage message = new MqttMessage(messageContent.getBytes());
            message.setQos(1);
            sampleClient.publish(topic, message);
            System.out.println("Message published from : " + clientId + " with payload of : " + messageContent);
        } catch (MqttException e) {
            e.printStackTrace();
            Assertions.fail("Sending a message should not have thrown any exception.");
        } finally {
            closeClient(sampleClient);
        }
    }

    public void closeClient(MqttClient client) {
        try {
            if (client != null) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Assertions.fail("There should be no error from Mqtt client!");
        }
    }
}
