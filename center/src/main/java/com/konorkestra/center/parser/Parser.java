package com.konorkestra.center.parser;

public interface Parser {
    public boolean parse(String yaml);
    public String getType();
}
