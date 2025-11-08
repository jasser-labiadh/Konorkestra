package com.konorkestra.center.parser;

import org.springframework.stereotype.Component;

@Component
public abstract class PolicyParser extends Parser{
    @Override
    public String getHandledKey() {
        return "policy";
    }
}
