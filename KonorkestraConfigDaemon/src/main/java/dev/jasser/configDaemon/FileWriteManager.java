package dev.jasser.configDaemon;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
// Component Overview:
// 1. **Metadata File**: Stores entries in the format: (config name, offset in data file, size, last transaction).
//    - This allows quick lookups to locate configuration values within the data file.
//
// 2. **Data File** (Append-Only): Stores the actual configuration values sequentially.
//    - Updates are appended to the end of this file for efficiency, avoiding in-place modifications.
//
// 3. **Index File**: Maps config names to their respective metadata offsets for fast lookups.
//    - Structure: (config name, offset in metadata file).
//    - Used to quickly find metadata entries without scanning the entire metadata file.
//    - On startup, the system rebuilds the in-memory hashmap from this file.

// Update Mechanism:
// - Updates append new values to the **data file** and update the **offset in the metadata file**.
// - Metadata updates involve modifying offsets, ensuring minimal overhead.
// - Locking is used to prevent inconsistent reads during updates.
//
// File Compaction Strategy:
// - Compaction removes unused entries by rewriting the data file from the **beginning** up to the last used offset.
// - This helps in reclaiming space efficiently.
//
// Indexing & HashMap:
// - A hashmap in memory maps `config name → offset in metadata file` for quick lookups.
// - On startup, the hashmap is rebuilt from the metadata file.
//
// Concurrency & Locking:
// - A **read-write lock** ensures that processes do not read during updates.
// - Updates involve locking the **data file**, appending the new value, and updating the **metadata file**.
//
// Process Subscription & Change Notification:
// - Processes using the configuration can watch for metadata file changes.
// - Future enhancements can implement logic for automatic **reloads, restarts, or custom reactions** to config updates.
//
// Entry Formats:
// - **Metadata File**: (offset in data file, size, last transaction ID).
// - **Index (In-memory)**: (config name → offset in metadata file).
// - **Data File**: Stores only the config values.
//
public class FileWriteManager implements WriteManager<String, String> {
    private LockManager lockManager ;
    private LogManager logManager ;
    private HashMap<String, Long> metaDataIndex; // key - offset in metadata file
    private RandomAccessFile metaDataFile;// format: offset, last transaction, type
    private RandomAccessFile DataFile;   // format: value
    private RandomAccessFile IndexFile; // format: key,offset

    public Boolean put(String key, String value) {
        long metaDataOffset = metaDataIndex.computeIfAbsent(key, k -> {
            try{
                // this should wetire first to the index file and then populate the hashmap

                return IndexFile.getFilePointer();
            }catch (IOException e){
                logManager.logError("Index file error");
                throw new RuntimeException(e);
            }
        });
        try{
            lockManager.getWriteLock(key);
            long dataOffset = DataFile.getFilePointer();
            DataFile.write(value.getBytes());
            metaDataFile.seek(metaDataOffset);
            metaDataFile.writeLong(dataOffset);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    public String get(String key) {
        // look for offset in hashmap -> look for offset in metadata -> get the value
        try{
            lockManager.getReadLock(key);
            long metaDataOffset = metaDataIndex.get(key);
            metaDataFile.seek(metaDataOffset);
            String line = metaDataFile.readLine();
            if (line == null) {
                return null;
            }
            long dataOffset = Long.valueOf(line.substring(0, line.indexOf(",")));
            DataFile.seek(dataOffset);
            return DataFile.readLine();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    private  long get_metaData_offset(String key) {
        if(!metaDataIndex.containsKey(key)){
            try{
                long pos = metaDataFile.getFilePointer();
                metaDataIndex.put(key, pos);
                return pos;
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        return metaDataIndex.get(key);
    }
    private void buildHashMap() {
        metaDataIndex = new HashMap<>();
        try {
            String line;
            IndexFile.seek(0);  // Move the file pointer to the beginning

            while ((line = IndexFile.readLine()) != null) {  // Read one line at a time
                String[] split = line.split(",");
                if (split.length == 2) {  // Ensure the line is well-formed
                    metaDataIndex.put(split[0], Long.parseLong(split[1]));  // Add to the hashmap
                } else {
                    // Handle the case where the line is malformed (optional)
                    System.err.println("Malformed line: " + line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading index file: " + e.getMessage(), e);
        }
    }

}
