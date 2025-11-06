package com.konorkestra.center.model;
import java.util.Map;
public class Policy {
    private String uid;
    private String type;
    private Map<String,Object> parameters;
    
    public Policy() {}
    public Policy(String uid, String type, Map<String, Object> parameters) {
        this.uid = uid;
        this.type = type;
        this.parameters = parameters;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
