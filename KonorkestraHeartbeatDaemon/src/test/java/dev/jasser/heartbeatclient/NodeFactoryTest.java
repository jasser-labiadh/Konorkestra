package dev.jasser.heartbeatclient;

import dev.jasser.heartbeatclient.CloudDetectors.CloudDetector;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NodeFactoryTest {

    @Test
    void testCloudProviderDetected() {
        CloudDetector mockDetector = () -> new String[]{"aws","123"};
        Node node = NodeFactory.createNode(Collections.singletonList(mockDetector));

        assertNotNull(node.getCloudName());
    }

    @Test
    void testNoCloudProviderDetected() {
        CloudDetector nullDetector = () -> null;
        Node node = NodeFactory.createNode(Collections.singletonList(nullDetector));

        assertNull(node.getCloudName(), "Node should have no provider ID when detection fails.");
        assertNotNull(node.getUID());
    }
}