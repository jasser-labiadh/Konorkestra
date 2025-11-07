package com.konorkestra.center.parser;

public interface Parser {
    public String getHandledKey ();
    public default boolean parse(Object obj) {
        return false;
    };
}
