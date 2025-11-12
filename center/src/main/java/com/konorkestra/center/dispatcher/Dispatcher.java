package com.konorkestra.center.dispatcher;
import com.konorkestra.center.model.Job;
/**
 * Generalizes the concept of a dispatcher.
 * A dispatcher is responsible for dispatching a request/Job
 * 
 * Input: dispatchable job 
 * Output: true if dispatch is successful 
 */
public interface Dispatcher {
    /**
     * Dispatch a job for execution
     * @param job
     * @return true of dispatch is successful
     */
    boolean dispatch(Job job);
}
