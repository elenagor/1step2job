package com.ostj.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.convertors.DataMapper;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;

public class PersonManager {
    private static Logger log = LoggerFactory.getLogger(PersonManager.class);

	private SQLAccess dbConnector;

    public PersonManager(String jdbcUrl, String username, String password) throws Exception {
        log.info("Start PersonManager");
        this.dbConnector = new SQLAccess(jdbcUrl, username, password );
    }

    public void getPersonData(int personId, Person person) throws Exception {
        String sqlQuery ="SELECT persons.*, profiles.resume, profiles.title, profiles.id AS profile_id FROM persons "+//
        "JOIN profiles ON profiles.person_id = persons.id WHERE profiles.person_id =  ? ;";

        List<Object> parameters = Arrays.asList(personId );
         try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }

    public void getPersonByProfileId( int ProfileId, Person person) throws Exception {
        String sqlQuery ="SELECT persons.*, profiles.resume, profiles.title, profiles.id AS profile_id FROM persons "+//
                        "JOIN profiles ON profiles.person_id = persons.id WHERE profiles.id = ?;";

        List<Object> parameters = Arrays.asList( ProfileId);
        try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person Profile by Id=%d", ProfileId));
        }
    }

    public void getPersonData(int personId, int ProfileId, Person person) throws Exception {
        String sqlQuery ="SELECT persons.*, profiles.resume, profiles.title, profiles.id AS profile_id FROM persons "+//
                        "JOIN profiles ON profiles.person_id = persons.id WHERE profiles.person_id =  ? AND profiles.id = ?;";

        List<Object> parameters = Arrays.asList(personId , ProfileId);
        try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }
    public List<Person>  getPersonByTitle(String title) throws Exception {
        List<Person> list = new ArrayList<Person>();
        String sqlQuery ="SELECT persons.*, profiles.resume, profiles.title, profiles.id AS profile_id FROM persons " + //
                        "JOIN profiles ON profiles.person_id = persons.id  WHERE profiles.title ~* ? ;";

        List<Object> parameters = Arrays.asList( title );
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);
        
        if(res != null){
            for (Map<String, Object> rs : res) {
                Person person = new Person();
                addProfilePerson( rs,  person);
                list.add(person);
            }
        }
        return list;
    }

    private void getPersonData(String sqlQuery, List<Object> parameters, Person person) throws Exception {
        List<Map<String, Object>> res = dbConnector.query(sqlQuery, parameters);

        if(res != null){
            for (Map<String, Object> rs : res) {
                addProfilePerson(rs, person);
            }
        }

        if(person.id < 0){
            throw new Exception(String.format("Error in query ", sqlQuery));
        }
    }

    private void addProfilePerson(Map<String, Object> rs, Person person ) throws Exception{
        DataMapper.convertToObject( rs, person, person.getClass() );
        Profile profile = new Profile();
        DataMapper.convertToObject( rs, profile, profile.getClass() );
        profile.id = (int) rs.get("profile_id");
        profile.person_id = (int) rs.get("id");
        person.profiles.add(profile);
    }


}
