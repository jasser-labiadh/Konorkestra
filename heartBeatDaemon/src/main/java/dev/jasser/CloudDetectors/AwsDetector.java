package dev.jasser.CloudDetectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Properties;

import dev.jasser.LogManager;

public class AwsDetector implements CloudDetector {
    private final String metadataUrl;
    private final String tokenUrl;
    private final HttpClient client;
    private final LogManager logManager;
    private final static Properties properties = new Properties();
    static {
        try (FileInputStream input = new FileInputStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // Constructor with default URLs
    public AwsDetector() {
        this(properties.getProperty("AWS_METADATA_URL"), properties.getProperty("AWS_TOKEN_URL"));
    }

    // Constructor with custom URLs
    public AwsDetector(String metadataUrl, String tokenUrl) {
        this.metadataUrl = metadataUrl;
        this.tokenUrl = tokenUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        this.logManager = LogManager.getInstance();
    }

    @Override
    public String[] detect() {
        String token = getToken();
        if (token == null) {
            logManager.logError("Failed to retrieve token for AWS metadata.");
            return null;
        }

        return fetchMetadata(token);
    }

    private String getToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("X-aws-ec2-metadata-token-ttl-seconds", "21600")
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                logManager.logError("Failed to retrieve token. Status Code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logManager.logError("Error while retrieving AWS token: " + e.getMessage());
        }

        return null;
    }

    private String[] fetchMetadata(String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metadataUrl))
                .timeout(Duration.ofSeconds(1))
                .header("X-aws-ec2-metadata-token", token)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new String[]{"AWS", response.body()};
            } else {
                logManager.logError("Failed to fetch metadata. Status Code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logManager.logError("Error while fetching AWS metadata: " + e.getMessage());
        }

        return null;
    }
}