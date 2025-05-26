package com.ostj.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;


public class PromptManager {
    private static Logger log = LoggerFactory.getLogger(PromptManager.class);
    private Connection conn;

    public PromptManager(String jdbcUrl, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public String getPrompt(ResumeProcessEvent event) throws Exception{
        if(event.promptFilePath != null){
            return Utils.getPromptByFileName(event.promptFilePath);
        }
        else{
            return getPromptById(event.PromptId);
        }
    }

    public String getPromptById(int promptId) throws SQLException{
        String sqlQuery ="select * from public.\"Prompts\" where public.\"Prompts\".\"Id\" = ?;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, promptId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("text"); 
        }
        return null;
    }
}
