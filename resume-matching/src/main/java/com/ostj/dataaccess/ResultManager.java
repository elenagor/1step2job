package com.ostj.dataaccess;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.ostj.dataentity.Result;

public class ResultManager {
    private static Logger log = LoggerFactory.getLogger(ResultManager.class);
    Gson gson = new Gson();

    @Autowired
	SQLAccess dbConnector;

    public ResultManager(){
        log.trace("Start ResultManager");
    }

    public ResultManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public int saveMatchResult(Result result) throws Exception {
        String insertQuery = "INSERT INTO results(person_id, profile_id, job_id, match_result_score, date, reasoning, details)VALUES (?, ?, ?, ?, ?, ?, ? ::json);";

        java.sql.Date sqlDate = new java.sql.Date(result.date.getTime());
        String details = gson.toJson(result.key_arias_of_comparison);

        List<Object> parameters = Arrays.asList( result.PersonId, result.ProfileId, result.JobId, result.overall_score, sqlDate, result.Reasoning, details );

        return dbConnector.update( insertQuery, parameters);
    }

    public void deleteMatchResult(int resultId) throws Exception {
        String query = "DELETE FROM results WHERE id = ? ;";
        log.debug("Start query DB: {}", query);

        List<Object> parameters = Arrays.asList( resultId );
        dbConnector.update(query, parameters) ;
    }
}
