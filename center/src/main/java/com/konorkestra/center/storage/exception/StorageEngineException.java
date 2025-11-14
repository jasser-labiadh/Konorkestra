package com.konorkestra.center.storage.exception;

public class StorageEngineException extends Exception {
    public StorageEngineException(String message) {
        super(message);
    }

    public StorageEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}