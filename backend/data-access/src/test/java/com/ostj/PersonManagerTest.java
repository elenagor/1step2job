package com.ostj;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entities.Person;
import com.ostj.managers.PersonManager;

public class PersonManagerTest {
    private static Logger log = LoggerFactory.getLogger(PersonManagerTest.class);

    private String jdbcUrl = "jdbc:postgresql://localhost:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testGetUserInfo1() throws Exception {
		PersonManager personManager = new PersonManager(jdbcUrl, username, password);
		Person person = new Person();
		personManager.getPersonData(1, person);
		assertTrue(person != null);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		log.trace("Response: {}", person.toString());
		assertTrue(person.profiles.get(0).person_id == 1);
	}

	@Test
	public void testGetUserInfo2() throws Exception {
		PersonManager personManager = new PersonManager(jdbcUrl, username, password);
		Person person = new Person();
		personManager.getPersonData(1, 1, person);
		assertTrue(person != null);
		assertTrue(person.id == 1);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() != 0);
		log.trace("Response: {}", person.toString());
		assertTrue(person.profiles.get(0).person_id == 1);
		log.trace("Response: {}", person.profiles.get(0).toString());
	}
}
