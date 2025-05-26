package com.ostj.dataentity;

public class Resume
{
    public int Id = -1;
    public int PersonId = -1;
    public String Content = "";

    public String toString(){
        return String.format("Id=%d, PersonId=%s, Content=%s", Id, PersonId, Content );
    }
}