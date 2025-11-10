package com.konorkestra.center.parser;

public abstract class Parser<I, T> {
    public abstract T parse(I input);
    public abstract Class<T> getParsedType();
    public abstract String getHandledKey();
}