package com.konorkestra.center.parser;

import org.springframework.stereotype.Component;

@Component
public class GroupParser implements Parser {
    @Override
    public String getHandledKey() {
        return "group";
    }
}
