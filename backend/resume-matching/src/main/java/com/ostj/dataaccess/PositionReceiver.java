package com.ostj.dataaccess;

import com.ostj.entities.Position;

import com.ostj.managers.PositionManager;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class PositionReceiver {

    private PositionManager dbConnector = null;
    
    public PositionReceiver(String jdbcUrl, String username, String password) throws Exception {
        dbConnector = new PositionManager(jdbcUrl,  username,  password);
    }

    public Position getJob(ResumeProcessEvent event) throws Exception{
        Position job = new Position();
        if(event.jdFilePath != null && event.jdFilePath.length() > 0){
            job.description = Utils.getFileContent(event.jdFilePath);
        }
        else{
            if(event.PositionExternalId != null && event.PositionExternalId.length() > 0){
                dbConnector.getJobFromDB(event.PositionExternalId,  job);
            }
            else{
                dbConnector.getJobFromDB(event.PositionId,  job);
            }
        }
        return job;
    }
}
