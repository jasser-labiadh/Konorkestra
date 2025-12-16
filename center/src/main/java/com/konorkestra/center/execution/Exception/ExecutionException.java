package com.konorkestra.center.execution.Exception;

public class ExecutionException extends Exception{
    public ExecutionException(String message){
        super(message);
    }
    public ExecutionException(String message, Throwable cause){
        super(message,cause);
    }
}
