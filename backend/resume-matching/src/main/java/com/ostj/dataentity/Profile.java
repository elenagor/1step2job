package com.ostj.dataentity;

public class Profile
{
    public int id = -1;
    public int person_id = -1;
    public String title = "";
    public String resume = "";

    public String toString(){
        return String.format("Id=%d, PersonId=%s, Title=%s, Content=%s", id, person_id, title, resume );
    }
}