package com.konorkestra.center.model;

import com.google.protobuf.Message;

public interface Persistable {

    /**
     * Unique key for this object in storage.
     */
    String getKey();

    /**
     * Serialize to Protobuf Message.
     */
    byte[] serialize();
}