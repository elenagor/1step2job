package com.ostj.dataproviders;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.ostj.convertors.DataMapper;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.MatchResult;

public class MatchResultProvider {
    private static Logger log = LoggerFactory.getLogger(MatchResultProvider.class);
    Gson gson = new Gson();
	private SQLAccess dbConnector;

    public MatchResultProvider(String jdbcUrl, String username, String password) throws Exception {
        log.info("Start MatchResultProvider");
        this.dbConnector = new SQLAccess(jdbcUrl, username, password );
    }

    public int saveMatchResult(MatchResult result) throws Exception {
        String insertQuery = "INSERT INTO person_position_matches(person_id, profile_id, position_id, score, date, reasoning, comparison_details)VALUES (?, ?, ?, ?, ?, ?, ? ::json);";

        java.sql.Date sqlDate = new java.sql.Date(result.date.getTime());
        String details = gson.toJson(result.key_arias_of_comparison);

        List<Object> parameters = Arrays.asList( result.Person_Id, result.Profile_Id, result.Position_Id, result.overall_score, sqlDate, result.Reasoning, details );

        return dbConnector.update( insertQuery, parameters);
    }

    public void getMatchResultForPerson(int personId,  MatchResult result) throws Exception{
        String sqlQuery ="SELECT * FROM person_position_matches WHERE person_id = ? ;";
        List<Object> parameters = Arrays.asList( personId );
        getMatchResult(sqlQuery, parameters,  result);
        if(result.Id < 0 ){
            throw new Exception(String.format("There is no match result by person id=%d", personId));
        }
    }

    public void getMatchResultByProfileId( int profileId,  MatchResult result) throws Exception{
        String sqlQuery ="SELECT * FROM person_position_matches WHERE profile_id = ? ;";
        List<Object> parameters = Arrays.asList( profileId );
        getMatchResult(sqlQuery, parameters,  result);
        if(result.Id < 0 ){
            throw new Exception(String.format("There is no match result by profile id={}", profileId));
        }
    }

    public void getMatchResultByPosition( int positionId,  MatchResult result) throws Exception{
        String sqlQuery ="SELECT * FROM person_position_matches WHERE  position_id = ? ;";
        List<Object> parameters = Arrays.asList(  positionId );
        getMatchResult(sqlQuery, parameters,  result);
        if(result.Id < 0 ){
            throw new Exception(String.format("There is no match result by  position id={}",  positionId));
        }
    }

    public void getMatchResult(int personId, int profileId,  MatchResult result) throws Exception{
        String sqlQuery ="SELECT * FROM person_position_matches WHERE person_id = ? AND profile_id = ? ;";
        List<Object> parameters = Arrays.asList( personId, profileId );
        getMatchResult(sqlQuery, parameters,  result);
        if(result.Id < 0 ){
            throw new Exception(String.format("There is no match result by person id=%d, profile id={}", personId, profileId));
        }
    }

    public void getMatchResult(int personId, int profileId, int positionId,  MatchResult result) throws Exception{
        String sqlQuery ="SELECT * FROM person_position_matches WHERE person_id = ? AND profile_id = ? AND position_id = ? ;";
        List<Object> parameters = Arrays.asList( personId, profileId, positionId );
        getMatchResult(sqlQuery, parameters,  result);
        if(result.Id < 0 ){
            throw new Exception(String.format("There is no match result by person id=%d, profile id={}, position id={}", personId, profileId, positionId));
        }
    }

    private void getMatchResult( String sqlQuery, List<Object> parameters, MatchResult result) throws Exception{
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);
        if (res != null) {
            for (Map<String, Object> rs : res) {
                DataMapper.convertToObject( rs, result, result.getClass() );
            }
        }
    }

    public int updateMatchResult(boolean value, String condition) throws SQLException {
        String insertQuery = "UPDATE person_position_matches SET  is_sent = ? WHERE id in (?) ;";

        List<Object> parameters = Arrays.asList( value, condition );

        return dbConnector.update( insertQuery, parameters);
    }
}
