package com.konorkestra.center.parser;

import org.springframework.stereotype.Component;

@Component
public abstract class GroupParser extends Parser {
    @Override
    public String getHandledKey() {
        return "group";
    }
}
