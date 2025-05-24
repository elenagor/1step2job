package com.ostj.resumeprocessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MatcherTest {
	 private static Logger log = LoggerFactory.getLogger(MatcherTest.class);
	Matcher matcher = new Matcher("EMPY", "http://localhost:8000/v1", "qwen");
	
	@Test
	public void testGetUserInfo() throws Exception {
		String resume = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("Person.txt"),  "UTF-8");
		String prompt =  IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("prompt_get_info.txt"),  "UTF-8");
		String response = matcher.call_openai(resume, "", prompt);
		log.trace("Response: " + response);
		assertTrue(response.contains("Sergei Azarkhin"));
		assertTrue(response.contains("sergei.azarkhin@gmail.com"));
	}
}
