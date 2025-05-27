package com.ostj.dataaccess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        log.trace("Start PromptManager");
    }

    public PromptManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public String getPrompt(ResumeProcessEvent event) throws Exception{
        if(event.promptFilePath != null && event.promptFilePath.length() > 0){
            return Utils.getPromptByFileName(event.promptFilePath);
        }
        else{
            return getPromptById(event.PromptId);
        }
    }

    private String getPromptById(int promptId) throws Exception {
        String prompt = null;
        String sqlQuery ="SELECT * FROM prompts WHERE id = ?;";

        List<Object> parameters = Arrays.asList(promptId );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if (res != null ) {
            prompt = (String) res.get(0).get("text"); 
        }

        if(prompt == null){
            throw new Exception(String.format("There is no prompt text by id=%d", promptId));
        }
        return prompt;
    }

}
