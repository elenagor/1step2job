package com.ostj.dataaccess;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.ostj.entities.MatchResult;


public class MatchResultManager {
    private static Logger log = LoggerFactory.getLogger(MatchResultManager.class);
    Gson gson = new Gson();

	private SQLAccess dbConnector;

    public MatchResultManager(){
        log.debug("Start MatchResultManager");
    }

    public MatchResultManager(String jdbcUrl, String username, String password) throws Exception{
        this.dbConnector = new SQLAccess(jdbcUrl, username, password);
    }

    public int updateMatchResult(MatchResult result) throws Exception {
        String insertQuery = "UPDATE person_position_matches "+//
        "SET  score = ?, date = ?, reasoning = ?, comparison_details = ? ::json " + //
        "WHERE person_id = ? AND profile_id = ? AND position_id = ? ;";

        java.sql.Date sqlDate = new java.sql.Date(result.date.getTime());
        String details = gson.toJson(result.key_arias_of_comparison);

        List<Object> parameters = Arrays.asList( result.overall_score, sqlDate, result.Reasoning, details,
            result.Person_Id, result.Profile_Id, result.Position_Id
         );

        return dbConnector.update( insertQuery, parameters);
    }

    public boolean isPersonProcessFinished(int person_id) throws SQLException{
        String query = "SELECT count(*) FROM person_position_matches JOIN persons ON persons.id = person_id WHERE person_id = ? AND score = 0 ;";
        List<Object> parameters = Arrays.asList(person_id);
        List<Map<String, Object>> res = dbConnector.query(query, parameters);
        long resultCount = 0;
        if(res != null){
            for (Map<String, Object> rs : res) {
                resultCount = (long) rs.get("count");
            }
        }
        return resultCount == 0 ? true : false;
    }

    public void deleteMatchResult(int resultId) throws Exception {
        String query = "DELETE FROM person_position_matches WHERE id = ? ;";
        log.debug("Start query DB: {}", query);

        List<Object> parameters = Arrays.asList( resultId );
        dbConnector.update(query, parameters) ;
    }
}
