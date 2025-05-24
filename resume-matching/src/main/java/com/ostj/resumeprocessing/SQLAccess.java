package com.ostj.resumeprocessing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entity.Resume;

public class SQLAccess {
    private static Logger log = LoggerFactory.getLogger(SQLAccess.class);

    private Connection conn;

    public SQLAccess(String jdbcUrl, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public Resume getPersonData(int PersonId) throws Exception {
        String sqlQuery =String.format( "SELECT Id, PersonId, Content FROM Resumes WHERE PersonId=%d", PersonId);
        log.debug("Start query DB: {}", sqlQuery);

        Statement stmt = this.conn.createStatement() ;
        ResultSet rs = stmt.executeQuery(sqlQuery);
        while (rs.next()) {
            Resume resume = new Resume(
                    rs.getInt("id"),
                    rs.getInt("PersonId"),
                    rs.getString("Content"));
            return resume;
        }
        throw new Exception(String.format("PersonId=%d doesn't exist", PersonId));
    }
}