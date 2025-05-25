package com.ostj.dataentity;

public class Resume
{
    public int Id;
    public int PersonId = 0;
    public String Content = "";

    public String toString(){
        return String.format("Id=%d, PersonId=%s, Content=%s", Id, PersonId, Content );
    }
}