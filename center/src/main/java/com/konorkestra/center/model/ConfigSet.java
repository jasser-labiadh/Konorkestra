package com.konorkestra.center.model;
import java.util.Map;
public class ConfigSet {
    private String uid;
    private Map<String,String> config;

    public ConfigSet() {}
    public ConfigSet(String uid, Map<String, String> config) {
        this.uid = uid;
        this.config = config;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public Map<String, String> getConfig() {
        return config;
    }
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
