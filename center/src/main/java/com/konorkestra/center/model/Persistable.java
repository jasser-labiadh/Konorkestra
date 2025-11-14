package com.konorkestra.center.model;

import com.google.protobuf.Message;

public interface Persistable<T> {

    /**
     * Unique key for this object in storage.
     */
    String getKey();

    /**
     * Serialize to Protobuf Message.
     */
    byte[] serialize();

    /**
     * Deserialize from Protobuf bytes.
     * Typically implemented as static in concrete class.
     */
    T deserialize(byte[] bytes);
}