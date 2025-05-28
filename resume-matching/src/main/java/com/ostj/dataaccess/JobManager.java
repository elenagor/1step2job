package com.ostj.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ostj.dataentity.Job;
import com.ostj.dataentity.Person;
import com.ostj.dataentity.Profile;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class JobManager {
    private static Logger log = LoggerFactory.getLogger(ResultManager.class);

    @Autowired
	SQLAccess dbConnector;

    public JobManager(){
        log.trace("Start JobManager");
    }

    public JobManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public Job getJob(ResumeProcessEvent event) throws Exception{
        Job job = new Job();
        if(event.jdFilePath != null && event.jdFilePath.length() > 0){
            job.description = Utils.getFileContent(event.jdFilePath);
        }
        else{
            if(event.JobExtId != null && event.JobExtId.length() > 0){
                getJobFromDB(event.JobExtId,  job);
            }
            else{
                getJobFromDB(event.JobId,  job);
            }
        }
        return job;
    }
    private void getJobFromDB(int JobId,  Job job) throws Exception{
        String sqlQuery ="SELECT * FROM jobs WHERE id = ?;";
        List<Object> parameters = Arrays.asList( JobId );
        getJobFromDB(sqlQuery, parameters,  job);
        if(job.id < 0 ){
            throw new Exception(String.format("There is no job by id=%d", JobId));
        }
    }
    private void getJobFromDB(String JobId,  Job job) throws Exception{
        String sqlQuery ="SELECT * FROM jobs WHERE external_id = ?;";
        List<Object> parameters = Arrays.asList(JobId );
        getJobFromDB(sqlQuery, parameters,  job);
        if(job.id < 0 ){
            throw new Exception(String.format("There is no job by external_id=%d", JobId));
        }
    }

    private void getJobFromDB( String sqlQuery, List<Object> parameters, Job job) throws Exception{
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);
        if (res != null) {
            for (Map<String, Object> rs : res) {
                Utils.convertToObject( rs, job, job.getClass() );
            }
        }
    }

    public List<Job> getJobs(Person person, Profile profile)  throws Exception {
        List<Job> list = new ArrayList<Job>();
        String sqlQuery ="SELECT * FROM jobs WHERE jobs.title ~* ? ;";

        List<Object> parameters = Arrays.asList(profile.title );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                Job job = new Job();
                Utils.convertToObject( rs, job, job.getClass());
                list.add(job);
            }
        }
        return list;
     }
}
