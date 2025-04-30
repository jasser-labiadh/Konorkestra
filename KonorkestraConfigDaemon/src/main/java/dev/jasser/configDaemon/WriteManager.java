package dev.jasser.configDaemon;
// this support persisting a config ang getting a config
// no miplementation needed ( if its local file, or we change the implementation np )
public interface WriteManager<Key, Value> {
    Boolean put(Key key, Value value);
    Value get(Key key);

}