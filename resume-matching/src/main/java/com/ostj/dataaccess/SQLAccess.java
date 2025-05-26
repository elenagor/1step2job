package com.ostj.dataaccess;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataentity.Job;
import com.ostj.dataentity.MatchResult;
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
            Job job = new Job();
            convertToObject( rs, job, job.getClass());
            return job;
        }
        return null;
    }

    public List<Job> getJobs(Person person)  throws Exception {
        List<Job> list = new ArrayList<Job>();
        PreparedStatement pstmt = createPersonConditions(person);

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            Job job = new Job();
            convertToObject( rs, job, job.getClass());
            list.add(job);
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

    public Person getPersonData(int PersonId) throws Exception {
        String sqlQuery ="select public.\"Persons\".*, public.\"Resumes\".\"Content\", public.\"Resumes\".\"Id\" as \"ResumeId\"";
        sqlQuery = sqlQuery + " from public.\"Persons\" join public.\"Resumes\" on public.\"Resumes\".\"PersonId\" = public.\"Persons\".\"Id\" where public.\"Resumes\".\"PersonId\" =  ? ;";
        log.debug("Start query DB: {}", sqlQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(sqlQuery) ;
        stmt.setInt(1, PersonId);

        Person person = new Person();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            addResumePerson(rs, person);
        }
        return person;
    }

    private void addResumePerson(ResultSet rs, Person person ) throws Exception{
        convertToObject( rs, person, person.getClass() );
        Resume resume = new Resume();
        resume.Id = rs.getInt("ResumeId");
        resume.PersonId = rs.getInt("Id");
        resume.Content = rs.getString("Content"); 
        person.resumes.add(resume);
    }

    public int saveMatchResult(MatchResult result) throws SQLException {
        String insertQuery = "INSERT INTO public.\"Results\"(\"PersonId\", \"ResumeId\", \"JobId\", \"MatchResultScore\")VALUES (?, ?, ?, ?);";
        log.debug("Start query DB: {}", insertQuery);

        PreparedStatement  stmt = this.conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS) ;
        stmt.setInt(1, result.PersonId);
        stmt.setInt(2, result.ResumeId);
        stmt.setInt(3, result.JobId);
        stmt.setInt(4, result.overall_score);

        int insertedRow = stmt.executeUpdate();
        if (insertedRow > 0) {
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public void deleteMatchResult(int resultId) throws SQLException {
        String query = "DELETE FROM public.\"Results\" WHERE \"Id\"= ? ;";
        log.debug("Start query DB: {}", query);

        PreparedStatement  stmt = this.conn.prepareStatement(query) ;
        stmt.setInt(1, resultId);

        stmt.executeQuery();
    }

    private <T> void convertToObject(ResultSet rs, T obj, Class<?> classType) throws Exception{
        for(Field fieldname : classType.getDeclaredFields()){
            setValue( fieldname, obj, rs);
        }
    }
    private <T> void setValue(Field fieldname, T obj, ResultSet rs){
        try{
            fieldname.setAccessible(true);
            if( fieldname.getType() == String.class ){
                fieldname.set(obj, rs.getString(fieldname.getName()) );
            } 
            if( fieldname.getType() == int.class ){
                fieldname.set(obj, rs.getInt(fieldname.getName()) );
            }
        }
        catch(Exception e){
            log.error("set field error {}", e);
        }
    }
}