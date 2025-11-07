package com.konorkestra.center.service;

import org.springframework.stereotype.Component;

/**
 * basically checks if the ConfigSet already exists --MVP
 */
@Component
public class ConfigSetValidator implements validator{
    
    
    public boolean validate(){
        return true;
    }
}
