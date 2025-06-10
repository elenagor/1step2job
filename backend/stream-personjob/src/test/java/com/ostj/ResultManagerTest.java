package com.ostj;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ostj.dataaccess.MatchResultsNotifyBulder;
import com.ostj.entities.Person;


public class ResultManagerTest {
    private static Logger log = LoggerFactory.getLogger(ResultManagerTest.class);
	private String jdbcUrl = "jdbc:postgresql://db.1step2job.ai:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";

    @Test
	public void testCreateEmailBody() throws Exception{
		MatchResultsNotifyBulder resultManager = new MatchResultsNotifyBulder(jdbcUrl, username, password);
		Person person = new Person();
        person.id = 1;
        String emailBody = resultManager.createEmailBody( person, 0);
        log.trace("emailBody: {}", emailBody);
    }
}
