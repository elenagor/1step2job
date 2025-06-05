package com.ostj.dataaccess;

import com.ostj.dataproviders.PositionProvider;
import com.ostj.entities.Position;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class PositionReceiver {

    private PositionProvider dbConnector = null;
    
    public PositionReceiver(String jdbcUrl, String username, String password) throws Exception {
        dbConnector = new PositionProvider(jdbcUrl,  username,  password);
    }

    public Position getPosition(ResumeProcessEvent event) throws Exception{
        Position job = new Position();
        if(event.jdFilePath != null && event.jdFilePath.length() > 0){
            job.description = Utils.getFileContent(event.jdFilePath);
        }
        else{
            if(event.PositionExternalId != null && event.PositionExternalId.length() > 0){
                dbConnector.getPositionFromDB(event.PositionExternalId,  job);
            }
            else{
                dbConnector.getPositionFromDB(event.PositionId,  job);
            }
        }
        return job;
    }
}
