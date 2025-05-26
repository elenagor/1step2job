package com.ostj.dataaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;


public class PromptManager {
    private static Logger log = LoggerFactory.getLogger(PromptManager.class);

    @Autowired
	SQLAccess dbConnector;

    public PromptManager(){

    }

    public PromptManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public String getPrompt(ResumeProcessEvent event) throws Exception{
        if(event.promptFilePath != null && event.promptFilePath.length() > 0){
            return Utils.getPromptByFileName(event.promptFilePath);
        }
        else{
            return dbConnector.getPromptById(event.PromptId);
        }
    }

}
