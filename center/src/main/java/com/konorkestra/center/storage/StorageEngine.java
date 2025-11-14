package com.konorkestra.center.storage;

import com.konorkestra.center.storage.exception.StorageEngineException;

import java.util.Map;

/**
 * Interface for all storage engine
 * main implementations: rocksDBStorageEngine 
 * 
 */
public interface StorageEngine<T> {
    public Boolean store(T data) throws StorageEngineException;
    public T retrieve(String key) throws StorageEngineException;
    public Boolean delete(String key) throws StorageEngineException;
}
