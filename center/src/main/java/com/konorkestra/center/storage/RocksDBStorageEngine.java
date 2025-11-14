package com.konorkestra.center.storage;

import com.konorkestra.center.storage.exception.StorageEngineException;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;

import java.util.*;

@Slf4j
public class RocksDBStorageEngine implements StorageEngine {

    private final RocksDB db;
    private final Map<String, ColumnFamilyHandle> cfHandles = new HashMap<>();

    static {
        RocksDB.loadLibrary();
    }

    /**
     * Opens RocksDB and initializes the given column families (namespaces).
     *
     * @param path           RocksDB storage path
     * @param columnFamilies List of column family names (namespaces)
     * @throws RocksDBException if RocksDB fails to open
     */
    public RocksDBStorageEngine(String path, List<String> columnFamilies) throws RocksDBException {
        List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
        cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

        for (String cfName : columnFamilies) {
            cfDescriptors.add(new ColumnFamilyDescriptor(cfName.getBytes(), new ColumnFamilyOptions()));
        }

        List<ColumnFamilyHandle> handles = new ArrayList<>();
        db = RocksDB.open(
                new DBOptions()
                        .setCreateIfMissing(true)
                        .setCreateMissingColumnFamilies(true),
                path, cfDescriptors, handles
        );

        for (int i = 0; i < cfDescriptors.size(); i++) {
            cfHandles.put(new String(cfDescriptors.get(i).getName()), handles.get(i));
        }
    }

    /** Store data in the given namespace */
    @Override
    public Boolean store(String namespace, String key, byte[] data) throws StorageEngineException {
        ColumnFamilyHandle cf = cfHandles.get(namespace);
        if (cf == null) throw new StorageEngineException("Unknown namespace: " + namespace);
        try {
            db.put(cf, key.getBytes(), data);
            return true;
        } catch (Exception e) {
            throw new StorageEngineException("Failed to store data with key: " + key, e);
        }
    }

    /** Retrieve data from the given namespace */
    @Override
    public byte[] retrieve(String namespace, String key) throws StorageEngineException {
        ColumnFamilyHandle cf = cfHandles.get(namespace);
        if (cf == null) throw new StorageEngineException("Unknown namespace: " + namespace);
        try {
            return db.get(cf, key.getBytes());
        } catch (Exception e) {
            throw new StorageEngineException("Failed to retrieve data with key: " + key, e);
        }
    }

    /** Delete data from the given namespace */
    @Override
    public Boolean delete(String namespace, String key) throws StorageEngineException {
        ColumnFamilyHandle cf = cfHandles.get(namespace);
        if (cf == null) throw new StorageEngineException("Unknown namespace: " + namespace);
        try {
            db.delete(cf, key.getBytes());
            return true;
        } catch (Exception e) {
            throw new StorageEngineException("Failed to delete data with key: " + key, e);
        }
    }

    /** Close all RocksDB resources */
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