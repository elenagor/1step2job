package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ostj.entities.Person;
import com.ostj.entities.Position;
import com.ostj.managers.PersonManager;
import com.ostj.managers.PositionManager;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
    public void calcTrashhold() throws Exception {
        PersonManager personMan = new PersonManager(jdbcUrl, username, password);
        PositionManager jobManager = new PositionManager(jdbcUrl, username, password);
        
        Person person = new Person();
        personMan.getPersonData(1, 1, person);
		assertTrue(person.id == 1);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		assertTrue(person.profiles.get(0).id == 1);
		assertTrue(person.profiles.get(0).job_titles.size() != 0);

        Position job = new Position();
		jobManager.getJobFromDB(1, job);
        assertTrue(job.id == 1);
    }
}
