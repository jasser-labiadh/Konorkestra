package com.konorkestra.center.parser;

import org.springframework.stereotype.Component;

@Component
public class PolicyParser implements Parser{
    @Override
    public String getHandledKey() {
        return "policy";
    }
}
