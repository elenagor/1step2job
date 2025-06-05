package com.ostj.dataaccess;

import com.ostj.dataproviders.PersonProvider;
import com.ostj.entities.Person;
import com.ostj.entities.Profile;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;


public class PersonReceiver {

    private PersonProvider dbConnector = null;
    
    public PersonReceiver(String jdbcUrl, String username, String password) throws Exception {
        dbConnector = new PersonProvider(jdbcUrl,  username,  password);
    }
        
    public Person getPersonData(ResumeProcessEvent event) throws Exception{
        Person person = new Person();
        if(event.resumeFilePath != null && event.resumeFilePath.length() > 0){
            Profile resume = new Profile();
            resume.resume = Utils.getFileContent(event.resumeFilePath);
            person.profiles.add(resume);
        }
        else{
            if(event.ProfileId >= 0 ){
                dbConnector.getPersonData(event.PersonId, event.ProfileId, person );
            }
            else{
                dbConnector.getPersonData(event.PersonId, person );
            }
            
        }
        return person;
    }
}
