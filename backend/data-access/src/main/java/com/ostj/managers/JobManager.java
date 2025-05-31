package com.ostj.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.convertors.DataMapper;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.Job;

public class JobManager {
    private static Logger log = LoggerFactory.getLogger(JobManager.class);

	private SQLAccess dbConnector;

    public JobManager(String jdbcUrl, String username, String password) throws Exception {
        log.info("Start JobManager");
        this.dbConnector = new SQLAccess(jdbcUrl, username, password );
    }

    public void getJobFromDB(int JobId,  Job job) throws Exception{
        String sqlQuery ="SELECT * FROM jobs WHERE id = ?;";
        List<Object> parameters = Arrays.asList( JobId );
        getJobFromDB(sqlQuery, parameters,  job);
        if(job.id < 0 ){
            throw new Exception(String.format("There is no job by id=%d", JobId));
        }
    }
    public void getJobFromDB(String JobId,  Job job) throws Exception{
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
                DataMapper.convertToObject( rs, job, job.getClass() );
            }
        }
    }

    public List<Job> getJobsWithTitle(String title)  throws Exception {
        List<Job> list = new ArrayList<Job>();
        String sqlQuery ="SELECT * FROM jobs WHERE jobs.title ~* ? ;";

        List<Object> parameters = Arrays.asList( title );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                Job job = new Job();
                DataMapper.convertToObject( rs, job, job.getClass());
                list.add(job);
            }
        }
        return list;
     }
}
