package dev.jasser;

import dev.jasser.CloudDetectors.CloudDetector;
import dev.jasser.LogManager;
import java.util.ServiceLoader;

/**
 * NodeFactory is responsible for creating instances of the Node class.
 * It attempts to detect whether the application is running in a cloud environment
 * by using the ServiceLoader mechanism to dynamically load available CloudDetector services.
 *
 * 1. ServiceLoader: This utility loads implementations of the CloudDetector interface.
 *    It allows the system to be flexible in terms of cloud detection by using different detectors.
 *
 * 2. Cloud Detection Logic: The NodeFactory iterates over each available CloudDetector implementation
 *    to detect if the environment is a known cloud provider (e.g., AWS, Azure).
 *    If a valid cloud provider ID is detected, it creates and returns a Node with that ID.
 *
 * 3. Fallback: If no cloud provider ID is detected (i.e., no CloudDetector provides a result),
 *    it falls back to creating a Node instance without a cloud provider ID,
 *    assuming the application is running on a local machine or unknown environment.
 */
public class NodeFactory {
    private static final LogManager logManager = LogManager.getInstance();

    static Node createNode(Iterable<CloudDetector> detectors) {
        for (CloudDetector detector : detectors) {
            String[] id = detector.detect();
            if (id != null) {
                logManager.logInfo("Cloud provider detected: " + id[0]);
                return new Node(id);
            }
        }
        logManager.logInfo("No cloud provider detected, defaulting to local environment.");
        return new Node(null);
    }

    static public Node createNode() {
        return createNode(ServiceLoader.load(CloudDetector.class));
    }
}