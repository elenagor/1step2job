package com.ostj.dataaccess;

import com.ostj.entities.Job;

import com.ostj.managers.JobManager;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class JobsReceiver {

    private JobManager dbConnector = null;
    
    public JobsReceiver(String jdbcUrl, String username, String password) throws Exception {
        dbConnector = new JobManager(jdbcUrl,  username,  password);
    }

    public Job getJob(ResumeProcessEvent event) throws Exception{
        Job job = new Job();
        if(event.jdFilePath != null && event.jdFilePath.length() > 0){
            job.description = Utils.getFileContent(event.jdFilePath);
        }
        else{
            if(event.JobExtId != null && event.JobExtId.length() > 0){
                dbConnector.getJobFromDB(event.JobExtId,  job);
            }
            else{
                dbConnector.getJobFromDB(event.JobId,  job);
            }
        }
        return job;
    }
}
