package dev.jasser.configDaemon;

public class ConfigMetaData {
    String key, value;
    long offset;
    long size;
    int lastTransaction;
    ConfigMetaData(String key, String value, long offset, int lastTransaction) {
        this.key = key;
        this.value = value;
        this.offset = offset;
        this.lastTransaction = lastTransaction;
        this.size = value.getBytes().length;
    }
}
