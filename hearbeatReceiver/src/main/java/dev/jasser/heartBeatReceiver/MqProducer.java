package dev.jasser.heartBeatReceiver;

import dev.jasser.heartBeatReceiver.proto.Heartbeat;

public interface MqProducer {
    public void send(String topic, Heartbeat heartbeat);
}
