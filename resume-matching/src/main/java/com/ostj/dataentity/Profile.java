package com.ostj.dataentity;

public class Profile
{
    public int Id = -1;
    public int PersonId = -1;
    public String Title = "";
    public String Content = "";

    public String toString(){
        return String.format("Id=%d, PersonId=%s, Title=%s, Content=%s", Id, PersonId, Title, Content );
    }
}