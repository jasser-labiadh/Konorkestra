package com.konorkestra.center.model;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Queue;

/**
 * 
 */
@Getter
@Setter
public class ExecutionPlan {
    private final Queue<Executable> queue;
    private final List<Executable> executed;
    
    public ExecutionPlan(Queue<Executable> queue) {
        this.queue = queue;
        this.executed = new java.util.ArrayList<>();
    }
}
