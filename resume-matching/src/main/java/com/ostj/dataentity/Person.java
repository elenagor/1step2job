package com.ostj.dataentity;

import java.util.ArrayList;
import java.util.List;

public class Person {
    public int Id = -1;
    public String Name = "";
    public String Email = "";
    public String Phone = "";
    public String City = "";
    public String State = "";
    public List<Profile> resumes;

    public Person(){
        this.resumes = new ArrayList<Profile>();
    }

    public String toString(){
        return String.format("Id=%d", Id )+", Name="+Name+", Email="+Email+", Phone="+Phone+", City="+City+", State="+State+", resumes count="+resumes.size();
    }
}
