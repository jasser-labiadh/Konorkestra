
/**
 * main storage engine for KONORKESTRA OSS
 * No support for multi-node: no built-in replication and leader election
 * USING ROCKSDB storage engine
 */
package com.konorkestra.center.storage;

import org.rocksdb.*;
import com.konorkestra.center.model.Persistable;

import java.util.*;

public class RocksDBStorageEngine<T extends Persistable<T>> implements StorageEngine<T> {

    private RocksDB db;
    private Map<String, ColumnFamilyHandle> cfHandles = new HashMap<>();
    private Class<T> clazz;

    static {
        RocksDB.loadLibrary();
    }

    public RocksDBStorageEngine(String path, Class<T> clazz, List<String> columnFamilies) throws RocksDBException {
        this.clazz = clazz;

        List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
        cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

        for (String cfName : columnFamilies) {
            cfDescriptors.add(new ColumnFamilyDescriptor(cfName.getBytes(), new ColumnFamilyOptions()));
        }

        List<ColumnFamilyHandle> handles = new ArrayList<>();
        db = RocksDB.open(new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true),
                path, cfDescriptors, handles);

        for (int i = 0; i < cfDescriptors.size(); i++) {
            cfHandles.put(new String(cfDescriptors.get(i).getName()), handles.get(i));
        }
    }

    private byte[] serialize(T obj) {
        return obj.toProto();
    }

    private T deserialize(byte[] bytes) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        return instance.fromProto(bytes);
    }

    @Override
    public Boolean store(T data) {
        try {
            String key = data.getKey();
            db.put(cfHandles.get(data.getClass().getSimpleName()), key.getBytes(), serialize(data));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public T retrieve(String key) {
        try {
            byte[] bytes = db.get(cfHandles.get(clazz.getSimpleName()), key.getBytes());
            if (bytes == null) return null;
            return deserialize(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            db.delete(cfHandles.get(clazz.getSimpleName()), key.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean batchStore(Map<String, T> data) {
        try (WriteBatch batch = new WriteBatch(); WriteOptions options = new WriteOptions()) {
            ColumnFamilyHandle cf = cfHandles.get(clazz.getSimpleName());
            for (Map.Entry<String, T> entry : data.entrySet()) {
                batch.put(cf, entry.getKey().getBytes(), serialize(entry.getValue()));
            }
            db.write(options, batch);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, T> batchRetrieve(Iterable<String> keys) {
        Map<String, T> result = new HashMap<>();
        for (String key : keys) {
            T value = retrieve(key);
            if (value != null) result.put(key, value);
        }
        return result;
    }

    @Override
    public Boolean batchDelete(Iterable<String> keys) {
        try (WriteBatch batch = new WriteBatch(); WriteOptions options = new WriteOptions()) {
            ColumnFamilyHandle cf = cfHandles.get(clazz.getSimpleName());
            for (String key : keys) {
                batch.delete(cf, key.getBytes());
            }
            db.write(options, batch);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        for (ColumnFamilyHandle handle : cfHandles.values()) {
            handle.close();
        }
        db.close();
    }
}