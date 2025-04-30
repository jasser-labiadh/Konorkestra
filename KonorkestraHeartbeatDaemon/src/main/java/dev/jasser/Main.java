package dev.jasser;
import dev.jasser.LogManager;
import dev.jasser.Node;
import dev.jasser.NodeFactory;

public class Main {
    public static void main(String[] args) {
        // Create the Node instance
        LogManager logManager = LogManager.getInstance();
        Node node = NodeFactory.createNode();
        // Start the heartbeat task
        node.startHeartbeat();

        // Add shutdown hook to gracefully stop heartbeat on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logManager.logInfo("Shutting down");
            node.stopHeartbeat(); // Stop the heartbeat gracefully
        }));
    }
}