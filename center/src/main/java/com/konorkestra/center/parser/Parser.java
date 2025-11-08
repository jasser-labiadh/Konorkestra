package com.konorkestra.center.parser;

import com.konorkestra.center.model.Persistable;

public abstract class Parser<I, T> {
    public abstract T parse(I input);
    public abstract Class<T> getParsedType();
    public abstract String getHandledKey();
}