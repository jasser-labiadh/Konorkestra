package com.konorkestra.center.storage;

import com.konorkestra.center.storage.exception.StorageEngineException;

/**
 * Generic interface for a storage engine.
 * Supports basic CRUD operations in the context of a "namespace".
 * The storage engine supports the concept of namespaces (or column families in RocksDB) to separate 
 * different types of data. This allows per-namespace optimizations, such as tuning performance, caching, 
 * or storage settings according to the specific requirements of each data type.
 * Repositories or higher-level services interact with this interface
 * and do not need to know the underlying storage implementation.
 */
public interface StorageEngine {

    /**
     * Store data in the given namespace with the specified key.
     *
     * @param namespace logical grouping of keys (e.g., "jobs", "configsets")
     * @param key       unique key for the object
     * @param data      serialized bytes
     * @return true if stored successfully
     * @throws StorageEngineException in case of storage failure
     */
    Boolean store(String namespace, String key, byte[] data) throws StorageEngineException;

    /**
     * Retrieve data by key from the given namespace.
     *
     * @param namespace logical grouping of keys
     * @param key       unique key
     * @return byte array of the stored object, or null if not found
     * @throws StorageEngineException in case of retrieval failure
     */
    byte[] retrieve(String namespace, String key) throws StorageEngineException;

    /**
     * Delete a key from the given namespace.
     *
     * @param namespace logical grouping of keys
     * @param key       unique key
     * @return true if deleted successfully, false if key did not exist
     * @throws StorageEngineException in case of deletion failure
     */
    Boolean delete(String namespace, String key) throws StorageEngineException;
}