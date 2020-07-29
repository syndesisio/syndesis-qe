package io.syndesis.qe.utils.mqtt;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Receiver implements MqttCallback {

    private String clientIdPrefix;
    public static int RECEIVED_FLAG = 0;

    public Receiver(String clientIdPrefix) {
        this.clientIdPrefix = clientIdPrefix;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Consumer connection lost : " + throwable.getMessage());
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        System.out.println("Message arrived from topic : " + s + " | Message : "
                + new String(mqttMessage.getPayload()) + " | Message ID : " + mqttMessage.getId());
        RECEIVED_FLAG = 1;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery completed from : " + clientIdPrefix);
    }
}
