package com.ostj.dataaccess;

import com.ostj.entities.Position;

import com.ostj.managers.PositionManager;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class JobsReceiver {

    private PositionManager dbConnector = null;
    
    public JobsReceiver(String jdbcUrl, String username, String password) throws Exception {
        dbConnector = new PositionManager(jdbcUrl,  username,  password);
    }

    public Position getJob(ResumeProcessEvent event) throws Exception{
        Position job = new Position();
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
