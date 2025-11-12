package com.konorkestra.center.repository;
import com.konorkestra.center.model.Job;
import java.util.List;

public interface JobRepository {
    public List<Job> getAllJobs();
    public Job getJobById(String id);
    public boolean saveJob(Job job);
    public boolean updateJob(Job job);
    public boolean deleteJob(String id);
}
