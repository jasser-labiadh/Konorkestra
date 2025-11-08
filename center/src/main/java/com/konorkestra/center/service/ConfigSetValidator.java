package com.konorkestra.center.service;

import org.springframework.stereotype.Component;

/**
 * basically checks if the ConfigSet already exists --MVP
 */
@Component
public class ConfigSetValidator implements validator<String>{
    
    
    public boolean validate(String uid){
        return true;
    }
}
