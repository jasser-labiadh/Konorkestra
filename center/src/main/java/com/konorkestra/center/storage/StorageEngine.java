package com.konorkestra.center.storage;

import java.util.Map;

/**
 * Interface for all storage engine
 * main implementations: rocksDBStorageEngine 
 * 
 */
public interface StorageEngine<T> {
    public Boolean store(T data);
    public T retrieve(String key);
    public Boolean delete(String key);
    public Boolean batchStore(Map<String,T> data);
    public Map<String,T> batchRetrieve(Iterable<String> keys);
    public Boolean batchDelete(Iterable<String> keys);
}
