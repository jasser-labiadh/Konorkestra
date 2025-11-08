package com.konorkestra.center.model;

import java.util.List;

public class Group{
    private String uid;
    private List<String> configsetUids; // references to ConfigSets
    private String policyUid;            // reference to Policy

    // resolved at runtime
    private List<ConfigSet> configSets;
    private Policy policy;

    // Constructors
    public Group() {}

    public Group(String uid, List<String> configsetUids, String policyUid) {
        this.uid = uid;
        this.configsetUids = configsetUids;
        this.policyUid = policyUid;
    }
    
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public List<String> getConfigsetUids() { return configsetUids; }
    public void setConfigsetUids(List<String> configsetUids) { this.configsetUids = configsetUids; }

    public String getPolicyUid() { return policyUid; }
    public void setPolicyUid(String policyUid) { this.policyUid = policyUid; }

    public List<ConfigSet> getConfigSets() { return configSets; }
    public void setConfigSets(List<ConfigSet> configSets) { this.configSets = configSets; }

    public Policy getPolicy() { return policy; }
    public void setPolicy(Policy policy) { this.policy = policy; }
}