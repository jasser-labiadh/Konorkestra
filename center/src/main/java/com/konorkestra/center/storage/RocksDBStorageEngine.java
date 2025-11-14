package com.konorkestra.center.storage;

import com.konorkestra.center.storage.exception.StorageEngineException;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import com.konorkestra.center.model.Persistable;
import java.util.*;
import java.util.function.Function;

/**
 * Generic RocksDB storage engine for any object implementing Persistable<T>.
 * Relies on polymorphism: the engine operates only on the Persistable interface,
 * allowing it to persist any type without knowing its concrete implementation.
 *
 * Domain-specific repositories should wrap this engine to implement higher-level logic,
 * such as job or configset operations, while keeping this layer generic.
 *
 * @param <T> Type of object to persist, must implement Persistable<T>
 */
@Slf4j
public class RocksDBStorageEngine<T extends Persistable<T>> implements StorageEngine<T> {

    private final RocksDB db;
    private final Map<String, ColumnFamilyHandle> cfHandles = new HashMap<>();
    private final Class<T> clazz;
    static {
        RocksDB.loadLibrary();
    }

    /**
     * 
     * @param path
     * @param columnFamilies
     * @throws RocksDBException
     * Basically initializes the ROCKSDB instance and create column families to separate them for our use case
     * prefix will be used for querying data inside one column family 
     */
    public RocksDBStorageEngine(String path, List<String> columnFamilies, Class<T> clazz)
            throws RocksDBException {
        this.clazz = clazz;
        List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
        cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions())); // default options for now, later I'll change this if needed

        for (String cfName : columnFamilies) {
            cfDescriptors.add(new ColumnFamilyDescriptor(cfName.getBytes(), new ColumnFamilyOptions())); // same here
        }

        List<ColumnFamilyHandle> handles = new ArrayList<>();
        db = RocksDB.open(new DBOptions()
                        .setCreateIfMissing(true)
                        .setCreateMissingColumnFamilies(true),
                path, cfDescriptors, handles);

        for (int i = 0; i < cfDescriptors.size(); i++) {
            cfHandles.put(new String(cfDescriptors.get(i).getName()), handles.get(i));
        }
    }
    @Override
    public Boolean store(T data) throws StorageEngineException{
        return store(data, cfHandles.get(RocksDB.DEFAULT_COLUMN_FAMILY));
    }
    public Boolean store(T data, ColumnFamilyHandle cf) throws StorageEngineException {
        try{
            String key = data.getKey();
            db.put(cf,key.getBytes(),data.serialize());
            return true;
        }
        catch (Exception e){
            throw new StorageEngineException("Failed to store data with key: " + data.getKey(), e);
        }
    }
    @Override
    public T retrieve(String key) throws StorageEngineException{
        return retrieve(key, cfHandles.get(RocksDB.DEFAULT_COLUMN_FAMILY));
    }
    public T retrieve(String key, ColumnFamilyHandle cf) throws StorageEngineException{
        try{
            byte[] bytes = db.get(cf,key.getBytes());
            if(bytes==null) return null;
            T object = clazz.newInstance();
            object.deserialize(bytes);
            return object;
        }
        catch (Exception e ){
            throw new StorageEngineException("Failed to retrieve data with key: " + key, e);
        }
    }
    @Override
    public Boolean delete (String key) throws StorageEngineException{
        return delete(key, cfHandles.get(RocksDB.DEFAULT_COLUMN_FAMILY));
    }
    public Boolean delete (String key, ColumnFamilyHandle cf) throws StorageEngineException{
        try {
            db.delete(cf, key.getBytes());
            return true;
        } catch (Exception e) {
            throw new StorageEngineException("Failed to delete data with key: " + key, e);
        }
    }
    public void close() throws StorageEngineException {
        List<Exception> exceptions = new ArrayList<>();

        for (ColumnFamilyHandle handle : cfHandles.values()) {
            try {
                handle.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        try {
            db.close();
        } catch (Exception e) {
            exceptions.add(e);
        }

        if (!exceptions.isEmpty()) {
            StorageEngineException ex = new StorageEngineException("Failed to close RocksDB resources");
            exceptions.forEach(ex::addSuppressed);
            throw ex;
        }
    }
}