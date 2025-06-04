package com.ostj.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.convertors.DataMapper;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.Position;

public class PositionManager {
    private static Logger log = LoggerFactory.getLogger(PositionManager.class);

	private SQLAccess dbConnector;

    public PositionManager(String jdbcUrl, String username, String password) throws Exception {
        log.info("Start JobManager");
        this.dbConnector = new SQLAccess(jdbcUrl, username, password );
    }

    public void getJobFromDB(int JobId,  Position job) throws Exception{
        String sqlQuery ="SELECT * FROM positions WHERE id = ?;";
        List<Object> parameters = Arrays.asList( JobId );
        getJobFromDB(sqlQuery, parameters,  job);
        if(job.id < 0 ){
            throw new Exception(String.format("There is no job by id=%d", JobId));
        }
    }
    public void getJobFromDB(String JobId,  Position job) throws Exception{
        String sqlQuery ="SELECT * FROM positions WHERE external_id = ?;";
        List<Object> parameters = Arrays.asList(JobId );
        getJobFromDB(sqlQuery, parameters,  job);
        if(job.id < 0 ){
            throw new Exception(String.format("There is no job by external_id=%d", JobId));
        }
    }

    private void getJobFromDB( String sqlQuery, List<Object> parameters, Position job) throws Exception{
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);
        if (res != null) {
            for (Map<String, Object> rs : res) {
                DataMapper.convertToObject( rs, job, job.getClass() );
            }
        }
    }

    public List<Position> getJobsWithTitle(int PersonId, int JobId, int titleId, float embeding_match_treshhold)  throws Exception {
        List<Position> list = new ArrayList<Position>();
        String sqlQuery ="SELECT persons.id as person_id,profiles.id as profile_id,job_titles.id as job_title_id,positions.id as position_id " + //
                        "FROM persons " + //
                        "JOIN profiles ON profiles.person_id = persons.id " + //
                        "JOIN job_titles ON job_titles.profile_id = profiles.id " + //
                        "JOIN positions ON job_titles.embedding <=> positions.title_embeddings < ? " + //
                        "WHERE persons.id = ? and profiles.id = ? and job_titles.id = ? " + //
                        "ORDER BY (job_titles.embedding <=> positions.title_embeddings) ;";

        List<Object> parameters = Arrays.asList( embeding_match_treshhold, PersonId, JobId, titleId  );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                Position job = new Position();
                job.id = (int)rs.get("position_id");
                list.add(job);
            }
        }
        return list;
    }
    
}
