package com.konorkestra.center.scheduler;

import com.konorkestra.center.model.Job;

/**
 * Schedules jobs for execution.
 * Responsible for ensuring jobs run according to concurrency rules,
 * execution strategy, and any working set constraints.
 */
public interface Scheduler {

    /**
     * Accept a new job for scheduling.
     * The scheduler decides when to execute it.
     *
     * @param job the job to schedule
     */
    void schedule(Job job);

    /**
     * Start the scheduler loop.
     * Typically runs in its own thread or executor.
     */
    void start();

    /**
     * Stop the scheduler gracefully.
     * Ensures currently running jobs finish.
     */
    void stop();
}