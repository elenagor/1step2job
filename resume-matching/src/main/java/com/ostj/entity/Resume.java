package com.ostj.entity;

public class Resume
{
    public int Id = 0;
    public int PersonId = 0;
    public String Content = "";

    public Resume(int Id, int PersonId, String Content) {
        this.Id = Id;
        this.PersonId = PersonId;
        this.Content = Content;
    }

    public String toString(){
        return String.format("Id=%d, PersonId=%s, Content=%s", Id, PersonId, Content );
    }
}