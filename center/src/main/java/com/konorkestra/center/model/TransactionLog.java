package com.konorkestra.center.model;
import com.google.protobuf.*;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the basis for recovery
 * each entry maps to unit operations compiled by the executionEngine from the Job definition
 * every operation is redo safe, thus recovery will basically read the log backward and redo things, specifically for konorkestra this is efficient ( no write heavy workload )  
 */
@Getter
@Setter
public class TransactionLog implements Persistable{
    private String transactionId;
    private String transactionType; //
    @Override
    public String getKey() {
        return null;
    }
    @Override
    public byte[] serialize() {
       // use protobuf serializer
        return null;
    }
    
}
