package com.ostj.dataentity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Job {
    public int Id;
    public String ext_id;
    public String title;
    public String location;
    public String published;
    public String description;
    public String application_url;
    public String salary;
    public String remote;
    public String type;

    public Job(){

    }

    public Job(ResultSet rs) throws SQLException{
        this.Id = rs.getInt("Id");
        this.ext_id = rs.getString("ext_id"); 
        this.title = rs.getString("title"); 
        this.location = rs.getString("location"); 
        this.published = rs.getString("published"); 
        this.description = rs.getString("description"); 
        this.application_url = rs.getString("application_url"); 
        this.salary = rs.getString("salary"); 
        this.remote = rs.getString("remote"); 
        this.type = rs.getString("type");
    }
        
    public String toString(){
        return String.format("Id=%d", Id )+", ext_id="+ext_id+", title="+title+", location="+location+", published="+published+", application_url="+application_url;
    }
}
