package com.ostj.resumeprocessing;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import kotlin.text.Charsets;

public class Matcher {
	private static Logger log = LoggerFactory.getLogger(Matcher.class);

	String apiKey;
	String endpoint;
	String model;

	
	public Matcher(String apiKey, String endpoint, String model) {
		super();
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.model = model;
	}

	public String run_resume_matching(String resumeFilePath, String jdFilePath, String promptFilePath)
			throws Exception {
		System.out.println("Matcher: apiKey=" + apiKey + ",endpoint=" + endpoint + ",model=" + model);
		System.out.println("Matcher: resumeFilePath=" + resumeFilePath + ",jdFilePath=" + jdFilePath
				+ ",promptFilePath=" + promptFilePath);

		String resume = readFile(resumeFilePath);
		String job_description = readFile(jdFilePath);
		String prompt = readFile(promptFilePath);

		log.trace("Matcher: resume=" + resume);
		log.trace("Matcher: job_description=" + job_description);
		log.trace("Matcher: prompt=" + prompt);

		// return call_openai( resume, job_description, prompt);
		return call_openai(resume, job_description, prompt);
	}

	private static String readFile(String path) throws Exception {
		if (StringUtils.isEmpty(path)) {
			return "";
		}

		return Files.readString(Path.of(path), Charsets.UTF_8);
	}

	public String call_openai(String resume, String job_description, String prompt) throws Exception {
		String user_content = "";
		if (StringUtils.isEmpty(job_description)) {
			// Replace {resume} placeholder
			user_content = prompt.replace("{resume}", resume);
		} else {
			user_content = prompt.replace("{resume}", resume);
			user_content = prompt.replace("{job_description}", job_description);
		}

		log.trace("Matcher: user message=" + user_content);

		OpenAIClient client = OpenAIOkHttpClient.builder()
			    .apiKey(apiKey)
			    .baseUrl(endpoint)
			    .build();
		// ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new
		// InetSocketAddress("localhost", 8000));

		ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
			    .addUserMessage(user_content)
			    .model(model)
			    .build();
		ChatCompletion chatCompletion = client.chat().completions().create(params);
		return chatCompletion.choices().get(0).message().toString();
	}
}
