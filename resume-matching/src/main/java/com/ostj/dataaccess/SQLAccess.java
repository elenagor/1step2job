package com.ostj.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataentity.Job;
import com.ostj.dataentity.Person;
import com.ostj.dataentity.Resume;

public class SQLAccess {
    private static Logger log = LoggerFactory.getLogger(SQLAccess.class);

    private Connection conn;

    public SQLAccess(String jdbcUrl, String username, String password) throws SQLException {
        this.conn = DriverManager.getConnection(jdbcUrl, username, password);
    }

    public Job getJob(String JobId)  throws Exception {
        String sqlQuery ="select * from public.\"Jobs\" where ext_id = ?;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setString(1, JobId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new Job(rs);
        }
        return null;
    }

    public List<Job> getJobs(Person person)  throws Exception {
        List<Job> list = new ArrayList<Job>();
        PreparedStatement pstmt = createPersonConditions(person);

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            list.add(new Job(rs));
        }
        return list;
    }

    private PreparedStatement createPersonConditions(Person person) throws SQLException {
        String sqlQuery ="select * from public.\"Jobs\" ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        //stmt.setString(1, JobId);

        return stmt;
    }

    public List<Person> getPersonData(int PersonId) throws Exception {
        String sqlQuery ="select public.\"Persons\".*, public.\"Resumes\".\"Content\", public.\"Resumes\".\"Id\" as \"ResumeId\"";
        sqlQuery = sqlQuery + " from public.\"Persons\" join public.\"Resumes\" on public.\"Resumes\".\"PersonId\" = public.\"Persons\".\"Id\" where public.\"Resumes\".\"PersonId\" =  ? ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, PersonId);

        List<Person> list = new ArrayList<Person>();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(createPerson(rs));
        }
        return list;
    }

    private Person createPerson(ResultSet rs ) throws SQLException{
        Person person = new Person(rs);
        Resume resume = new Resume();
        resume.Id = rs.getInt("ResumeId");
        resume.PersonId = rs.getInt("Id");
        resume.Content = rs.getString("Content"); 
        person.resumes.add(resume);
        return person;
    }
}