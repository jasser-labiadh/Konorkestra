package com.konorkestra.center.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Job {

    private String name;
    private String author;
    private String description;
    private JobType type;        // Enum for type: CREATE_GROUP, UPDATE_CONFIG, DELETE_CONFIG, NODE_JOIN, REVOKE_TOKEN

    private Scope scope;
    private Payload payload;
    private Strategy strategy;

    // ---------------- Nested Classes ----------------

    @Getter
    @Setter
    public static class Scope {
        private String group;          // optional
        private String configset;      // optional
        private String key;            // optional
        private String nodeId;         // optional
        private String tokenId;        // optional
    }

    @Getter
    @Setter
    public static class Payload {
        private Map<String, Object> keyValues;      // key-value pairs for config updates
        private List<String> configsets;            // for group creation
        private String policy;                       // optional
    }

    @Getter
    @Setter
    public static class Strategy {
        private StrategyType type;                   // Enum: ALL, QUORUM, EVENTUAL, BATCH
        private Integer batchSize;                   // optional for BATCH
        private Integer waitInterval;                // optional for BATCH
        private Double quorum;                       // optional for QUORUM (percentage or number)
        private Map<String, String> constraints;     // optional, only for BATCH
    }

    // ---------------- Enums ----------------

    public enum JobType {
        CREATE_GROUP,
        UPDATE_CONFIG,
        DELETE_CONFIG,
        NODE_JOIN,
        REVOKE_TOKEN
    }

    public enum StrategyType {
        ALL,
        QUORUM,
        EVENTUAL,
        BATCH
    }
}
