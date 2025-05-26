package com.ostj.dataentity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Person {
    public int Id = -1;
    public String Name = "";
    public String Email = "";
    public String Phone = "";
    public String City = "";
    public String State = "";
    public List<Resume> resumes;

    public Person(){
        this.resumes = new ArrayList<Resume>();
    }

    public String toString(){
        return String.format("Id=%d", Id )+", Name="+Name+", Email="+Email+", Phone="+Phone+", City="+City+", State="+State+", resumes count="+resumes.size();
    }

    public Person(ResultSet rs) throws SQLException {
        this();
        this.Id = rs.getInt("Id");
        this.Name = rs.getString("Name"); 
        this.Email = rs.getString("Email"); 
        this.Phone = rs.getString("Phone"); 
        this.City = rs.getString("City"); 
        this.State = rs.getString("State"); 
    }
}
