package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ostj.openai.AIMatcher;
import com.ostj.utils.Utils;


public class AIMatcherTest {
	 private static Logger log = LoggerFactory.getLogger(AIMatcherTest.class);
	AIMatcher ai_matcher = new AIMatcher("EMPY", "http://localhost:8000/v1", "qwen");
	
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
/* 
	@Test
	public void testMatchResumeToJobDescription() throws Exception{
		String resume = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Person.txt"),  "UTF-8");
		String prompt = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("prompt.txt"),  "UTF-8");
		String job_description =  IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("JobDescription.txt"),  "UTF-8");
		String response = ai_matcher.call_openai( resume,  job_description,  prompt);
    	assertTrue(response != null);
        log.trace("AIMatcher Response: {}", response);
		String jsonString = Utils.getJsonContextAsString(response);
		JsonObject jsonValue = JsonParser.parseString(jsonString).getAsJsonObject();
		log.trace("Json Response: {}", jsonValue);
		String reasoning = Utils.getThinksAsText(response);
		log.trace("reasoning: {}", reasoning);
    }
*/

}
