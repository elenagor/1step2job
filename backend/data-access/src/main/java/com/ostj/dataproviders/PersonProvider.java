package com.ostj.dataproviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.convertors.DataMapper;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.entities.Person;
import com.ostj.entities.Job_title;
import com.ostj.entities.Profile;

public class PersonProvider {
    private static Logger log = LoggerFactory.getLogger(PersonProvider.class);
	private SQLAccess dbConnector;
    private static String QUERY_PERSON_FIELD = "persons.id as person_id " + 
                ",profiles.id as profile_id " + 
                ",job_titles.id as job_title_id " + 
                ",persons.name " + 
                ",persons.email " + 
                ",profiles.accept_remote " + 
                ",profiles.location_city " + 
                ",profiles.location_country " + 
                ",profiles.location_state_or_region " + 
                ",profiles.salary_min " + 
                ",profiles.salary_max " + 
                ",profiles.resume " + 
                ",job_titles.title " + 
                ",job_titles.embedding ";

    public PersonProvider(String jdbcUrl, String username, String password) throws Exception {
        log.info("Start PersonManager");
        this.dbConnector = new SQLAccess(jdbcUrl, username, password );
    }

    public void getPersonData(int personId, Person person) throws Exception {
        String sqlQuery = "SELECT " + QUERY_PERSON_FIELD + "FROM persons " + //
        "JOIN profiles ON profiles.person_id = persons.id "+//
        "JOIN job_titles ON job_titles.profile_id = profiles.id "+//
        "WHERE profiles.person_id =  ? ;";

        List<Object> parameters = Arrays.asList(personId );
         try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }

    public void getPersonByProfileId( int ProfileId, Person person) throws Exception {
        String sqlQuery ="SELECT "+ QUERY_PERSON_FIELD + "FROM persons " + //
                        "JOIN profiles ON profiles.person_id = persons.id "+//
                        "JOIN job_titles ON job_titles.profile_id = profiles.id "+//
                        "WHERE profiles.id = ?;";

        List<Object> parameters = Arrays.asList( ProfileId);
        try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person Profile by Id=%d", ProfileId));
        }
    }

    public void getPersonData(int personId, int ProfileId, Person person) throws Exception {
        String sqlQuery ="SELECT "+ QUERY_PERSON_FIELD + "FROM persons " + //
                        "JOIN profiles ON profiles.person_id = persons.id "+//
                        "JOIN job_titles ON job_titles.profile_id = profiles.id "+//
                        "WHERE profiles.person_id =  ? AND profiles.id = ?;";

        List<Object> parameters = Arrays.asList(personId , ProfileId);
        try{
            getPersonData( sqlQuery, parameters,  person);
        }
        catch(Exception e){
            throw new Exception(String.format("There is no person by id=%d", personId));
        }
    }

    public List<Person>  getPersonByTitle(int position_id, float embeding_match_treshhold) throws Exception {
        List<Person> list = new ArrayList<Person>();
        String sqlQuery ="SELECT persons.id as person_id, profiles.id as profile_id, job_titles.id as job_title_id " +//
                        "FROM positions " +//
                        "JOIN job_titles ON positions.title_embeddings <=> job_titles.embedding < ? " +//
                        "JOIN profiles ON job_titles.profile_id = profiles.id " +//
                        "JOIN persons ON profiles.person_id = persons.id " + //
                        "WHERE positions.id = ? "+//
                        "ORDER BY (positions.title_embeddings <=> job_titles.embedding) ;";

        List<Object> parameters = Arrays.asList( embeding_match_treshhold, position_id  );
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
        person.id = (int) rs.get("person_id");
        Profile profile = createProfileById( person, (int)rs.get("profile_id"));
        DataMapper.convertToObject( rs, profile, profile.getClass() );
        addProfileTitle(rs, profile);
    }
    
    private Profile createProfileById(Person person, int id) {
        Profile profile = null;
        for(Profile prfl : person.profiles){
            if(prfl.id == id)
            {
                profile = prfl;
                break;
            }
        }
        if(profile == null){
            profile = new Profile();
            person.profiles.add(profile);  
        }
        profile.id = id;
        return profile;
    }

    private void addProfileTitle(Map<String, Object> rs, Profile profile) throws Exception {
        Job_title title = new Job_title();
        DataMapper.convertToObject( rs, title, title.getClass() );
        title.id = (int) rs.get("job_title_id");
        profile.job_titles.add(title);
    }


}
