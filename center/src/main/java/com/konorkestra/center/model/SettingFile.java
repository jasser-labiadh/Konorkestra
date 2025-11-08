package com.konorkestra.center.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This represents the config File we pass to bootstrap 
 */
@Getter
@Setter
public class SettingFile{
    private List<Group> groups;
    private List<Policy> policies;
    private List<ConfigSet> configSets;
    private String uid;
}
