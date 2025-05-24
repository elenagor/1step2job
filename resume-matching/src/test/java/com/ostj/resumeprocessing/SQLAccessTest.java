package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ostj.entity.Resume;

public class SQLAccessTest {
    
	
	@Test
	public void testGetUserInfo() throws Exception {
		SQLAccess dbConnector = new SQLAccess("jdbc:postgresql://localhost:5432/ostjdb", "ostjuser", "ostjuser!");
		Resume resume = dbConnector.getPersonData(1);
		System.out.println("Response: " + resume);
		assertTrue(resume != null);
		assertTrue(resume.PersonId == 0);
	}
}
