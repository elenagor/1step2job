package com.ostj.dataaccess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        log.trace("Start PersonManager");
    }

    public PersonManager(SQLAccess dbConnector){
        this.dbConnector = dbConnector;
    }

    public Person getPersonData(ResumeProcessEvent event) throws Exception{
        Person person = new Person();
        if(event.resumeFilePath != null && event.resumeFilePath.length() > 0){
            Profile resume = new Profile();
            resume.resume = Utils.getPromptByFileName(event.resumeFilePath);
            person.profiles.add(resume);
        }
        else{
            getPersonData(event.PersonId, person );
        }
        return person;
    }
    
    private void getPersonData(int personId, Person person) throws Exception {
        String sqlQuery ="SELECT persons.*, profiles.resume, profiles.title, profiles.id AS profile_id FROM persons "+//
        "JOIN profiles ON profiles.person_id = persons.id WHERE profiles.person_id =  ? ;";

        List<Object> parameters = Arrays.asList(personId );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                addProfilePerson(rs, person);
            }
        }

        if(person.id < 0){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }

    private void addProfilePerson(Map<String, Object> rs, Person person ) throws Exception{
        Utils.convertToObject( rs, person, person.getClass() );
        Profile profile = new Profile();
        Utils.convertToObject( rs, profile, profile.getClass() );
        profile.id = (int) rs.get("profile_id");
        profile.person_id = (int) rs.get("id");
        person.profiles.add(profile);
    }
}
