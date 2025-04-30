package dev.jasser.configDaemon;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import dev.jasser.configDaemon.Data.JoinRequest;
import io.grpc.stub.StreamObserver;
import dev.jasser.configDaemon.DynamicConfigurationServiceGrpc.*;
import dev.jasser.configDaemon.Data.*;
public class GrpcClient {
    private final ManagedChannel channel;
    private final DynamicConfigurationServiceStub asyncStub;
    private final LogManager logManager;
    private StreamObserver<ClientMessage> requestObserver;
    private StreamObserver<ConfigurationUpdate> responseObserver;
    private final WriteManager writeManager;
    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .build();
        logManager = LogManager.getInstance();
        this.asyncStub = DynamicConfigurationServiceGrpc.newStub(channel);
        writeManager = new FileWriteManager();
    }
    public GrpcClient(ManagedChannel channel) {
        this.channel = channel;
        logManager = LogManager.getInstance();
        writeManager = new FileWriteManager();
        this.asyncStub = DynamicConfigurationServiceGrpc.newStub(channel);
    }
    public void getDynamicUpdates(String key) {
        // StreamObserver for receiving ConfigurationUpdates
        JoinRequest joinRequest = JoinRequest.newBuilder().setKey(key).build();
        responseObserver = new StreamObserver<ConfigurationUpdate>() {
            @Override
            public void onNext(ConfigurationUpdate configurationUpdate) {
                Boolean success = writeManager.put(configurationUpdate.getKey(), configurationUpdate.getValue());
                sendAcknowledgment(configurationUpdate.getTransactionId(),
                    success,
                            "temp" // this is the whole logic here of persiuting the configuratiom
            );
            }
            @Override
            public void onError(Throwable throwable) {
                logManager.logError("error handling the update");
                sendAcknowledgment(-1, false, "error");
            }

            @Override
            public void onCompleted() {
                return ;
            }
        };
        requestObserver = asyncStub.subscribe(responseObserver);
        requestObserver.onNext(ClientMessage.newBuilder().setJoinRequest(joinRequest).build()); // this i initiates the bidirectional stream
    }
    private void sendAcknowledgment (int transactionId, boolean success, String message) {
        Acknowledgment acknowledgment = Acknowledgment.newBuilder()
                .setTransactionId(transactionId)
                .setSuccess(success)
                .setMessage(message)
                .build();
        ClientMessage ackMessage = ClientMessage.newBuilder().setAcknowledgment(acknowledgment).build();
        requestObserver.onNext(ackMessage);
    }
}
