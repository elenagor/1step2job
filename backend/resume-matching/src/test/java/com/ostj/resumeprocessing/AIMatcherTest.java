package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ostj.utils.StrictEnumTypeAdapterFactory;
import com.ostj.utils.Utils;
import com.ostj.OpenAIProvider;
import com.ostj.dataaccess.PersonReceiver;
import com.ostj.dataaccess.PositionReceiver;
import com.ostj.dataaccess.ResultManager;
import com.ostj.dataaccess.SQLAccess;
import com.ostj.dataentity.MatchResult;
import com.ostj.entities.Person;
import com.ostj.entities.Position;
import com.ostj.resumeprocessing.events.ResumeProcessEvent;


public class AIMatcherTest {
	private static Logger log = LoggerFactory.getLogger(AIMatcherTest.class);
	private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();
	OpenAIProvider ai_matcher = new OpenAIProvider("EMPY", "http://localhost:8000/v1", "qwen");
	private String jdbcUrl = "jdbc:postgresql://db.1step2job.ai:5432/ostjdb";
	private String username = "ostjsvc";
	private String password = "ostjsvc!";
/* 
	@Test
	public void testGetUserInfo() throws Exception {
		String resume = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Person.txt"),  "UTF-8");
		String prompt =  IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"),  "UTF-8");
		String response = ai_matcher.call_openai(resume, "", prompt);
		assertTrue(response != null);
		log.trace("AIMatcher Response: {}", response);
		String jsonString = Utils.getJsonContextAsString(response);
		JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
		log.trace("Json Response: {}", jsonValue);
	}
*/	
	@Test
	public void testMatchResumeToJobDescription() throws Exception{
		PersonReceiver personReceiver = new PersonReceiver(jdbcUrl, username, password);
		PositionReceiver positionReceiver = new PositionReceiver(jdbcUrl, username, password);
		SQLAccess dbConnector = new SQLAccess(jdbcUrl, username, password);
		ResultManager resultManager = new ResultManager(dbConnector);
		ResumeProcessEvent record = new ResumeProcessEvent(1, "", 1047, -1, "prompt.txt");

		Person person = personReceiver.getPersonData(record);
		assertTrue(person != null);
		assertTrue(person.profiles != null);
		assertTrue(person.profiles.size() > 0);
		String resume = person.profiles.get(0).resume;
		
		String prompt =  IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("prompt.txt"),  "UTF-8");

		Position  position = positionReceiver.getPosition(record);
		assertTrue(position != null);
		String job_description =  position.description;

		String response = ai_matcher.call_openai( resume,  job_description,  prompt);
    	assertTrue(response != null);
        log.trace("AIMatcher Response: {}", response);
		String jsonString = Utils.getJsonContextAsString(response);
		JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
		log.trace("Json Response: {}", jsonValue);
		 
		MatchResult result = gson.fromJson(jsonValue, MatchResult .class);

		String emailBody = resultManager.createEmailBody(result, person, position);
		log.trace("Email Body: {}", emailBody);
    }

	
}
