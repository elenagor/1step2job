package com.ostj.resumeprocessing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entity.Job;
import com.ostj.entity.Person;
import com.ostj.entity.Resume;

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
            return createJobRecord(rs);
        }
        return null;
    }

    public List<Job> getJobs(Person person)  throws Exception {
        List<Job> list = new ArrayList();
        String sqlQuery ="select * from public.\"Jobs\" ;";
        log.debug("Start query DB: {}", sqlQuery);

        Statement stmt = this.conn.createStatement();
        ResultSet rs = stmt.executeQuery(sqlQuery);
        while (rs.next()) {
            list.add(createJobRecord(rs));
        }
        return list;
    }

    public List<Person> getPersonData(int PersonId) throws Exception {
        String sqlQuery ="select public.\"Persons\".*,public.\"Resumes\".\"Content\" from public.\"Persons\" join public.\"Resumes\" on public.\"Resumes\".\"PersonId\" = public.\"Persons\".\"Id\" where public.\"Resumes\".\"PersonId\" =  ? ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, PersonId);

        List<Person> list = new ArrayList();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(createPerson(rs));
        }
        return list;
    }

    private Person createPerson(ResultSet rs ) throws SQLException{
        Person person = new Person();
        person.Id = rs.getInt("Id");
        person.Name = rs.getString("Name"); 
        person.Email = rs.getString("Email"); 
        person.Phone = rs.getString("Phone"); 
        person.City = rs.getString("City"); 
        person.State = rs.getString("State"); 
        Resume resume = new Resume();
        resume.PersonId = rs.getInt("Id");
        resume.Content = rs.getString("Content"); 
        person.resumes.add(resume);
        return person;
    }

    private Job createJobRecord(ResultSet rs) throws SQLException{
        Job job = new Job();
        job.ext_id = rs.getString("ext_id"); 
        job.title = rs.getString("title"); 
        job.location = rs.getString("location"); 
        job.published = rs.getString("published"); 
        job.description = rs.getString("description"); 
        job.application_url = rs.getString("application_url"); 
        job.salary = rs.getString("salary"); 
        job.remote = rs.getString("remote"); 
        job.type = rs.getString("type"); 
        return job;
    }
}