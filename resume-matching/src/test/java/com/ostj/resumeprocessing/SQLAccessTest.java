package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ostj.entity.Person;

public class SQLAccessTest {
    
	@Test
	public void testGetUserInfo() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		List<Person> persons = dbConnector.getPersonData(1);
		assertTrue(persons != null);
		assertTrue(persons.size() > 0);
		assertTrue(persons.get(0).resumes != null);
		assertTrue(persons.get(0).resumes.size() != 0);
		System.out.println("Response: " + persons.get(0).resumes.get(0).Content);
		assertTrue(persons.get(0).resumes.get(0).PersonId == 1);
	}
}
