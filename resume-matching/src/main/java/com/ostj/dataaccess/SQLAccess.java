package com.ostj.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ostj.dataentity.Alignment;
import com.ostj.dataentity.Result;

public class SQLAccess {
    private static Logger log = LoggerFactory.getLogger(SQLAccess.class);

    private Connection conn;

    public SQLAccess(String jdbcUrl, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public String getPromptById(int promptId) throws Exception{
        String sqlQuery ="SELECT * FROM prompts WHERE id = ?;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, promptId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("text"); 
        }
        throw new Exception(String.format("There is no prompt text by id=%d", promptId));
    }

    public ResultSet getPersonData(int PersonId) throws Exception {
        String sqlQuery ="SELECT persons.*, resumes.content, resumes.title, resumes.id AS resumeid FROM persons "+//
        "JOIN resumes ON resumes.person_id = persons.id " + //
        "WHERE resumes.person_id =  ? ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, PersonId);

        return stmt.executeQuery();
    }

    public ResultSet getJob(String JobId)  throws Exception {
        String sqlQuery ="SELECT * FROM jobs WHERE ext_id = ?;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setString(1, JobId);

        return stmt.executeQuery();
    }

    public ResultSet getJobs(PreparedStatement pstmt)  throws Exception {
        return pstmt.executeQuery();
    }

    public PreparedStatement createQuerySearchByTitle(String title) throws SQLException {
        String sqlQuery ="SELECT * FROM jobs WHERE jobs.title ~* ? ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setString(1, title);

        return stmt;
    }

    public int saveMatchResult(Result result) throws SQLException {
        int savedId = -1;
        String insertQuery = "INSERT INTO results(person_id, resume_id, job_id, match_result_score, details)VALUES (?, ?, ?, ?, ?);";
        log.debug("Start query DB: {}", insertQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS) ;
        stmt.setInt(1, result.PersonId);
        stmt.setInt(2, result.ResumeId);
        stmt.setInt(3, result.JobId);
        stmt.setInt(4, result.overall_score);
        stmt.setString(5, StringUtils.join( result.key_arias_of_comparison, " "));

        int insertedRow = stmt.executeUpdate();
        if (insertedRow > 0) {
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                savedId = rs.getInt(1);
            }
        }
        return savedId;
    }

    public void deleteMatchResult(int resultId) throws SQLException {
        String query = "DELETE FROM results WHERE id= ? ;";
        log.debug("Start query DB: {}", query);

        PreparedStatement  stmt = this.conn.prepareStatement(query) ;
        stmt.setInt(1, resultId);

        stmt.executeQuery();
    }

}