package dev.jasser;

import dev.jasser.LogManager;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import dev.jasser.Data.*;
public class Node {
    private static final LogManager logManager = LogManager.getInstance();
    private static final Properties properties = new Properties();

    // Instance variables for heartbeat functionality
    private final String UID;
    private final ScheduledExecutorService scheduler;
    private final String cloudName;
    private final ManagedChannel channel;
    private final HeartbeatServiceGrpc.HeartbeatServiceStub asyncStub;

    // Static block to load properties
    static {
        try (FileInputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logManager.logError("Failed to load application.properties: {}"+e.getMessage());
        }
    }
    public static String getGrpcServerAddress() {
        return properties.getProperty("grpc.server.address", "localhost:50051"); // Default value if not set
    }
    public Node(String[] id) {
        if (id != null && id.length > 1) {
            this.cloudName = id[0];
            this.UID = id[0] + '-' + id[1];
        } else {
            this.cloudName = null;
            this.UID = getMacAddress(); // Fallback to MAC address if no cloud ID provided
        }

        // Initialize gRPC channel and async stub
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.channel = ManagedChannelBuilder.forTarget(getGrpcServerAddress()).useTransportSecurity().build();
        this.asyncStub = HeartbeatServiceGrpc.newStub(channel);
    }
    // Start sending heartbeats periodically
    public void startHeartbeat() {
        StreamObserver<Data.Heartbeat> requestObserver = asyncStub.sendHeartbeats(new StreamObserver<Data.Ack>() {
            @Override
            public void onNext(Data.Ack ack) {
                logManager.logError("Received ack from server: {}"+ack.getSuccess());
            }

            @Override
            public void onError(Throwable t) {
                logManager.logError("Error in heartbeat stream: {}"+t.getMessage());
            }

            @Override
            public void onCompleted() {
                logManager.logInfo("Heartbeat stream completed.");
            }
        });

        // Task to send heartbeat data
        Runnable heartbeatTask = () -> {
            Data.Heartbeat heartbeat = Data.Heartbeat.newBuilder()
                    .setUid(UID)
                    .setCloudName(cloudName != null ? cloudName : "")
                    .setTimestamp(System.currentTimeMillis())
                    .build();
            requestObserver.onNext(heartbeat); // Send heartbeat to server
        };
        scheduler.scheduleAtFixedRate(heartbeatTask, 0, 500, TimeUnit.MILLISECONDS);
        logManager.logInfo("Heartbeat scheduler started.");
    }

    // Stop the heartbeat and shutdown resources
    public void stopHeartbeat() {
        scheduler.shutdown(); // Shutdown the scheduler
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logManager.logWarn("Forcing heartbeat scheduler shutdown...");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            logManager.logError("Interrupted while waiting for scheduler shutdown: {}"+e.getMessage());
            scheduler.shutdownNow();
        }

        channel.shutdown(); // Shutdown the gRPC channel
        try {
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                logManager.logWarn("Forcing gRPC channel shutdown...");
                channel.shutdownNow();
            }
        } catch (InterruptedException e) {
            logManager.logError("Interrupted while waiting for gRPC channel shutdown: {}"+e.getMessage());
            channel.shutdownNow();
        }

        logManager.logInfo("Heartbeat scheduler and gRPC channel stopped.");
    }

    // Helper method to generate UID based on MAC address
    public static String generateUid() {
        return getMacAddress(); // Uses MAC address as a fallback UID
    }

    // Get the MAC address of the first active network interface
    public static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) { // Active and non-loopback interfaces
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder macAddress = new StringBuilder();
                        for (byte b : mac) {
                            macAddress.append(String.format("%02X", b)).append(":");
                        }
                        return macAddress.substring(0, macAddress.length() - 1); // Remove trailing colon
                    }
                }
            }
        } catch (SocketException e) {
            logManager.logError("Failed to get MAC address: {}"+e.getMessage());
        }
        return null; // Return null if no valid interface is found
    }

    // Getter for cloud name
    public String getCloudName() {
        return this.cloudName;
    }

    // Getter for UID
    public String getUID() {
        return UID;
    }
}