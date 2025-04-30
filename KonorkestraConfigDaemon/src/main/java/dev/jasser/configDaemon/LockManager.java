package dev.jasser.configDaemon;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockManager {
    // A map that holds read-write locks for each configuration key
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    // Acquires the read lock for the given key
    public void getReadLock(String key) {
        lockMap.computeIfAbsent(key, k -> new ReentrantReadWriteLock()).readLock().lock();
    }

    // Acquires the write lock for the given key
    public void getWriteLock(String key) {
        lockMap.computeIfAbsent(key, k -> new ReentrantReadWriteLock()).writeLock().lock();
    }

    // Releases the read lock for the given key
    public void releaseReadLock(String key) {
        ReentrantReadWriteLock lock = lockMap.get(key);
        if (lock != null && lock.isWriteLockedByCurrentThread()) {
            lock.readLock().unlock();
        }
    }

    // Releases the write lock for the given key
    public void releaseWriteLock(String key) {
        ReentrantReadWriteLock lock = lockMap.get(key);
        if (lock != null && lock.writeLock().isHeldByCurrentThread()) {
            lock.writeLock().unlock();
        }
    }
}