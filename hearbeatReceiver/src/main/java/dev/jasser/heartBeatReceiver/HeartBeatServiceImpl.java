package dev.jasser.heartBeatReceiver;
import dev.jasser.heartBeatReceiver.proto.Heartbeat;
import dev.jasser.heartBeatReceiver.proto.HeartbeatResponse;
import dev.jasser.heartBeatReceiver.proto.HeartbeatServiceGrpc;
import dev.jasser.heartBeatReceiver.proto.HeartbeatServiceGrpc.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
@GrpcService
public class HeartBeatServiceImpl extends HeartbeatServiceGrpc.HeartbeatServiceImplBase {
    private final static LogManager logManager = LogManager.getInstance();
    @Autowired
    private HeartBeatStorage storage;
    @Autowired
    private HeartBeatStorage heartBeatStorage;
    @Autowired
    private MqProducer mqProducer;
    @Override
    public StreamObserver<Heartbeat> sendHeartbeat(StreamObserver<HeartbeatResponse> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(Heartbeat heartbeat) {
                logManager.logInfo("Received: "+heartbeat.toString());
                // persist to redis
                // TODO: Listen to ket expiry
                // here we don t care where the mq producer is we jsut have a config file
                storage.saveHeartBeat(heartbeat.getNodeId(),String.valueOf(heartbeat.getTimestamp()));
                mqProducer.send("analyze", heartbeat);
                HeartbeatResponse ack = HeartbeatResponse.newBuilder()
                        .setAccepted(true)
                        .setMessage("Heartbeat received")
                        .build();

                responseObserver.onNext(ack);
            }

            @Override
            public void onError(Throwable t) {
                logManager.logError("stream error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logManager.logInfo("Client completed streaming");
                responseObserver.onCompleted();
            }
        };
    }
}