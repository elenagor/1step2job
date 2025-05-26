package com.ostj.dataaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ostj.dataentity.Person;
import com.ostj.dataentity.Resume;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;
import com.ostj.utils.Utils;

public class PersonManager {
    private static Logger log = LoggerFactory.getLogger(PersonManager.class);

    @Autowired
	SQLAccess dbConnector;

    public Person getPersonData(ResumeProcessEvent event) throws Exception{
        Person person = new Person();
        if(event.resumeFilePath != null){
            Resume resume = new Resume();
            resume.Content = Utils.getPromptByFileName(event.resumeFilePath);
            person.resumes.add(resume);
            return person;
        }
        else{
            person = dbConnector.getPersonData(event.PersonId );
        }
        return person;
    }
}
