package com.ostj.dataaccess;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.ostj.dataentity.MatchResult;

public class ResultManager {
    private static Logger log = LoggerFactory.getLogger(ResultManager.class);
    Gson gson = new Gson();

    @Autowired
	SQLAccess dbConnector;

    public ResultManager(){
        log.debug("Start ResultManager");
    }

    public ResultManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public int saveMatchResult(MatchResult result) throws Exception {
        String insertQuery = "INSERT INTO results(person_id, profile_id, position_id, score, date, reasoning, comparison_details)VALUES (?, ?, ?, ?, ?, ?, ? ::json);";

        java.sql.Date sqlDate = new java.sql.Date(result.date.getTime());
        String details = gson.toJson(result.key_arias_of_comparison);

        List<Object> parameters = Arrays.asList( result.Person_Id, result.Profile_Id, result.Position_Id, result.overall_score, sqlDate, result.Reasoning, details );

        return dbConnector.update( insertQuery, parameters);
    }

    public void deleteMatchResult(int resultId) throws Exception {
        String query = "DELETE FROM results WHERE id = ? ;";
        log.debug("Start query DB: {}", query);

        List<Object> parameters = Arrays.asList( resultId );
        dbConnector.update(query, parameters) ;
    }
}
