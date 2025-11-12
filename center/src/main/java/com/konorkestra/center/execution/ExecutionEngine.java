package com.konorkestra.center.execution;

import com.konorkestra.center.model.Job;

/**
 * ExecutionEngine is responsible for executing Jobs.
 * It takes a job, interprets its execution plan, and performs the required operations.
 * Handles batching, constraints, quorums, and atomic operations.
 */
public interface ExecutionEngine {

    /**
     * Execute a job according to its execution plan.
     * Should be thread-safe and handle idempotency.
     *
     * @param job the job to execute
     * @return true if execution succeeded, false otherwise
     */
    boolean execute(Job job);

    /**
     * Optionally, shutdown the engine gracefully.
     */
    void shutdown();
}