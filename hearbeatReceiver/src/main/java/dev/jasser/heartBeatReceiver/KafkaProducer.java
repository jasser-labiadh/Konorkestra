package dev.jasser.heartBeatReceiver;

import dev.jasser.heartBeatReceiver.proto.Heartbeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer implements MqProducer {

    private final KafkaTemplate<String, Heartbeat> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Heartbeat> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, Heartbeat heartbeat) {
        // Send the message to the given Kafka topic
        kafkaTemplate.send(topic, heartbeat);
    }
}
