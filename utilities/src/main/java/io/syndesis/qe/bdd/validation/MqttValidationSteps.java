package io.syndesis.qe.bdd.validation;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.mqtt.MqttUtils;
import io.syndesis.qe.utils.mqtt.Receiver;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;


@Slf4j
public class MqttValidationSteps {
    private MqttUtils mqttUtils = new MqttUtils();
    private static LocalPortForward mqttLocalPortForward = null;

    @Then("^.*verify that when message is sent to \"([^\"]*)\" topic it is redirected to \"([^\"]*)\" topic via integration$")
    public void sendAndRecieveMessage(String senderTopic, String receiverTopic) {

        //reset message received flag
        Receiver.RECEIVED_FLAG = 0;

        MqttClient receiverClient = null;
        try {
            portForward();
            // give port forward some time
            TestUtils.sleepIgnoreInterrupt(2000);
            //create receiver
            receiverClient = mqttUtils.createReceiver("receiver_1", receiverTopic);

            //send message via client
            mqttUtils.sendMessage("Hi from syndesis integration!", senderTopic);

            //give it some short time for delivery
            TestUtils.sleepIgnoreInterrupt(4000);

        } catch (MqttException e) {
            e.printStackTrace();
            Assertions.fail("Mqtt Exception was thrown during message transfer");
        } finally {
            //close receiver
            mqttUtils.closeClient(receiverClient);

            //close mqtt port
            portClose();
        }

        //check flag
        Assertions.assertThat(Receiver.RECEIVED_FLAG).as("Message was not received!!!")
                .isEqualTo(1);

    }

    private void portForward() {
        if (mqttLocalPortForward == null || !mqttLocalPortForward.isAlive()) {
            //can be the same pod twice forwarded to different ports? YES
            Pod pod = OpenShiftUtils.xtf().getAnyPod("app", "broker-amq");
            log.info("POD NAME: *{}*", pod.getMetadata().getName());
            mqttLocalPortForward = OpenShiftUtils.portForward(pod, 1883, 1883);
        }
    }

    private void portClose() {
        if (mqttLocalPortForward != null || mqttLocalPortForward.isAlive()) {
            try {
                mqttLocalPortForward.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Error while closing mqtt port forward.");
            }
        }
    }
}
