package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.ostj.entities.Person;
import com.ostj.managers.PersonManager;

public class PersonManagerTest {
    //private static Logger log = LoggerFactory.getLogger(PersonManagerTest.class);

    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testGetUserInfoByPersonId() throws Exception {
		PersonManager personManager = new PersonManager(jdbcUrl, username, password);
		Person person = new Person();
		personManager.getPersonData(1, person);
		assertTrue(person != null);
		assertTrue(person.id == 1);
	}

	@Test
	public void testGetUserInfoProfileId() throws Exception {
		PersonManager personManager = new PersonManager(jdbcUrl, username, password);
		Person person = new Person();
		personManager.getPersonByProfileId( 1, person);
		assertTrue(person != null);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() > 0);
		assertTrue(person.profiles.get(0).id == 1);
	}

	@Test
	public void testGetUserInfoByPersonIDAndProfileId() throws Exception {
		PersonManager personManager = new PersonManager(jdbcUrl, username, password);
		Person person = new Person();
		personManager.getPersonData(1, 1, person);
		assertTrue(person != null);
		assertTrue(person.id == 1);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		assertTrue(person.profiles.get(0).id == 1);
		assertTrue(person.profiles.get(0).person_id == 1);
	}
}
