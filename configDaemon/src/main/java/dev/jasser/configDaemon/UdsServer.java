package dev.jasser.configDaemon;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import dev.jasser.configDaemon.UdsServer.*;
/**
 * UdsServer - UNIX Domain Socket-based Configuration Daemon
 *
 * This component acts as a centralized configuration provider for multiple local processes.
 * It listens for requests over a UNIX Domain Socket (UDS) and allows clients to retrieve
 * configuration values in a structured manner using Protobuf messages.
 *
 * ## Key Responsibilities:
 * - Listens for configuration requests from local processes via UDS.
 * - Supports a **subscription-based model** where clients can subscribe to config changes.
 * - Maintains persistent client connections to enable **push-based notifications**.
 * - Notifies subscribed clients when a configuration update occurs, ensuring real-time consistency.
 * - Uses **non-blocking I/O** with the Selector API to efficiently handle multiple connections.
 * - **Clients cannot modify configurations**; updates originate from a centralized system. ( admin CLI / API )
 *
 * ## How It Works:
 * 1. Clients establish a connection to the UDS server.
 * 2. Clients send a request to **fetch configuration values**.
 * 3. Clients may also **subscribe to configuration changes**.
 * 4. When the config changes (detected internally), the server **pushes notifications** to all subscribers.
 * 5. Clients receive the notification and fetch the updated configuration asynchronously.
 *
 * ## Design Considerations:
 * - **No polling:** Push-based notifications eliminate unnecessary request overhead.
 * - **Efficient event-driven model:** Uses non-blocking I/O to scale with multiple clients.
 * - **Graceful disconnection handling:** Ensures robust error handling for disconnected clients.
 * - **Security-first approach:** Communication is limited to local processes via UDS.
 *
 */
public class UdsServer {
    private final LogManager logManager;
    private final WriteManager writeManager;
    private final Path socketPath;
    private final ClientWorkerPool clientWorkerPool;
    private final Set<SocketChannel> subscribers = ConcurrentHashMap.newKeySet();

    public UdsServer(LogManager logManager, WriteManager writeManager, Path socketPath, int threadPoolSize) {
        this.logManager = logManager;
        this.writeManager = writeManager;
        this.socketPath = socketPath;
        this.clientWorkerPool = new ClientWorkerPool(threadPoolSize);
    }

    public UdsServer() {
        this.logManager = LogManager.getInstance();
        this.writeManager = new FileWriteManager();
        this.socketPath = Paths.get("/var/run/konorkestra.sock");
        this.clientWorkerPool = new ClientWorkerPool(4);
    }

    public void listen() throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            serverSocketChannel.configureBlocking(false);
            UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);
            serverSocketChannel.bind(address);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        accept(serverSocketChannel, selector);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        clientWorkerPool.submit(() -> {
                            try {
                                readDataFromClient(channel, selector);
                            } catch (Exception e) {
                                logManager.logError("Error reading from client: " + e.getMessage());
                                cleanupClient(channel, selector);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            logManager.logError("Fatal error: " + e.getMessage());
        }
    }

    private void accept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            logManager.logInfo("New client connected: " + socketChannel);
        }
    }

    private void readDataFromClient(SocketChannel clientChannel, Selector selector) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            cleanupClient(clientChannel, selector);
            return;
        }

        buffer.flip();
        CodedInputStream codedInput = CodedInputStream.newInstance(buffer);
        UdsData.UdsRequest request = UdsData.UdsRequest.parseFrom(codedInput);
        switch (request.getType()) {
            case "FETCH" -> {
                String key = request.getKey();
                String value = (String)writeManager.get(key);
                sendDataToClient(clientChannel, value != null ? statusCodes.SUCCESS :statusCodes.ERROR_INTERNAL_SERVER, value, statusCodes.TYPE_FETCH);
            }
            case "SUBSCRIBE" -> {
                subscribers.add(clientChannel);
                sendDataToClient(clientChannel, statusCodes.TYPE_FETCH, "", statusCodes.SUBSCRIBE_OK);
                logManager.logInfo("Client subscribed: " + clientChannel);
            }
            default -> sendDataToClient(clientChannel, statusCodes.ERROR_UNKNOWN, "", statusCodes.ERROR_INVALID_REQUEST);
        }
    }

    private void sendDataToClient(SocketChannel clientChannel,int status, String value, int type) {
        try {
            UdsData.UdsResponse response = UdsData.UdsResponse.newBuilder()
                    .setStatusCode(status)
                    .setValue(value)
                    .setType(type)
                    .build();
            byte[] bytes = response.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                clientChannel.write(buffer);
            }
        } catch (IOException e) {
            logManager.logError("Failed to send response: " + e.getMessage());
            cleanupClient(clientChannel, null);
        }
    }

    private void cleanupClient(SocketChannel channel, Selector selector) {
        try {
            logManager.logInfo("Cleaning up client: " + channel);
            subscribers.remove(channel);
            SelectionKey key = selector != null ? channel.keyFor(selector) : null;
            if (key != null) key.cancel();
            channel.close();
        } catch (IOException e) {
            logManager.logError("Failed to clean up client: " + e.getMessage());
        }
    }

    /**
     * Call this method when configuration changes.
     * It will notify all subscribed clients.
     */
    public void notifySubscribers(String updatedConfigKey) {
        String newValue = (String)writeManager.get(updatedConfigKey);
        for (SocketChannel subscriber : subscribers) {
            sendDataToClient(subscriber, statusCodes.SUCCESS, newValue, statusCodes.TYPE_CHANGE_NOTIFICATION);
        }
    }
}
