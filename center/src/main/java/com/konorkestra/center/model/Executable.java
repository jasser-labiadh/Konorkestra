package com.konorkestra.center.model;

import com.konorkestra.center.execution.Exception.ExecutionException;

public interface Executable {
    public void execute() throws ExecutionException;
}
