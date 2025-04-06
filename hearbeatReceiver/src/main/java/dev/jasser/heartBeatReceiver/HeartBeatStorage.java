package dev.jasser.heartBeatReceiver;

public interface HeartBeatStorage {
    void saveHeartBeat(String nodeID, String heartBeatStatus);
    String getHeartBeatStatus(String nodeID);
}
