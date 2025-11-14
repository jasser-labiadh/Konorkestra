package com.konorkestra.center.transaction;

/**
 * Provides logging for Recovery 
 * will use internally the Storage engine 
 * main implementation for OSS: make use of column family in rocksDB
 * Redo is made possible because atomic operations in each job are redo-safe
 */
public interface TransactionEngine {
}
