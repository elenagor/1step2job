package com.ostj.dataaccess;

import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ostj.dataentity.Person;
import com.ostj.dataentity.Profile;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class PersonManager {
    private static Logger log = LoggerFactory.getLogger(PersonManager.class);

    @Autowired
	SQLAccess dbConnector;

    public PersonManager(){

    }

    public PersonManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public Person getPersonData(ResumeProcessEvent event) throws Exception{
        Person person = new Person();
        if(event.resumeFilePath != null && event.resumeFilePath.length() > 0){
            Profile resume = new Profile();
            resume.Content = Utils.getPromptByFileName(event.resumeFilePath);
            person.resumes.add(resume);
        }
        else{
            ResultSet rs = dbConnector.getPersonData(event.PersonId );
            while (rs.next()) {
                addProfilePerson(rs, person);
            }
        }
        return person;
    }
    
    private void addProfilePerson(ResultSet rs, Person person ) throws Exception{
        Utils.convertToObject( rs, person, person.getClass() );
        Profile profile = new Profile();
        Utils.convertToObject( rs, profile, profile.getClass() );
        profile.Id = rs.getInt("ResumeId");
        profile.PersonId = rs.getInt("id");
        person.resumes.add(profile);
    }
}
