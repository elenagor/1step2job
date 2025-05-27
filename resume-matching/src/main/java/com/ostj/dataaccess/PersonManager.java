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
            getPersonData(event.PersonId, person );
        }
        return person;
    }
    
    private void getPersonData(int personId, Person person) throws Exception {
        String sqlQuery ="SELECT persons.*, profile.content, profile.title, profile.id AS profile_id FROM persons "+//
        "JOIN profile ON profile.person_id = persons.id WHERE profile.person_id =  ? ;";

        List<Object> parameters = Arrays.asList(personId );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                addProfilePerson(rs, person);
            }
        }

        if(person.Id < 0){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }

    private void addProfilePerson(Map<String, Object> rs, Person person ) throws Exception{
        Utils.convertToObject( rs, person, person.getClass() );
        Profile profile = new Profile();
        Utils.convertToObject( rs, profile, profile.getClass() );
        profile.Id = (int) rs.get("profile_id");
        profile.PersonId = (int) rs.get("id");
        person.resumes.add(profile);
    }
}
