package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.entity.Job;
import com.ostj.entity.Person;

public class SQLAccessTest {
    private static Logger log = LoggerFactory.getLogger(SQLAccessTest.class);

	@Test
	public void testGetUserInfo() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		List<Person> persons = dbConnector.getPersonData(1);
		assertTrue(persons != null);
		assertTrue(persons.size() > 0);
		assertTrue(persons.get(0).resumes != null);
		assertTrue(persons.get(0).resumes.size() != 0);
		log.trace("Response: " + persons.get(0).resumes.get(0).Content);
		assertTrue(persons.get(0).resumes.get(0).PersonId == 1);
	}

	@Test
	public void testJob() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		Job job = dbConnector.getJob("9055748138");
		assertTrue(job != null);
		log.trace("Response: " + job.description);
	}
}
