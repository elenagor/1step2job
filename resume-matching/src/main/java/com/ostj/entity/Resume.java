package com.ostj.entity;

public class Resume
{
    public int PersonId = 0;
    public String Content = "";

    public String toString(){
        return String.format("PersonId=%s, Content=%s", PersonId, Content );
    }
}