package com.ostj;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class OpenAIProvider {
	private static Logger log = LoggerFactory.getLogger(OpenAIProvider.class);

	String apiKey;
	String endpoint;
	String model;

	public OpenAIProvider(String apiKey, String endpoint, String model) {
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.model = model;
	}

	public String call_openai(String resume, String job_description, String prompt) throws Exception {
		String user_content = "";
		if (job_description == null || job_description.length() == 0) {
			//  Prompt must contents {resume} placeholder
			user_content = prompt.replace("{resume}", resume);
		} else {
			// Prompt must contents {resume} placeholder
			user_content = prompt.replace("{resume}", resume);
			//  Prompt must contents {job_description} placeholder
			user_content = user_content.replace("{job_description}", job_description);
		}

		log.trace("AIMatcher: user message={}", user_content);

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
		return chatCompletion.choices().get(0).message().content().get();
	}

	public static String converResponseToJsonString(String text) {

        Pattern compiledPattern = Pattern.compile("^[^{]*(\\{.*\\})[^}]*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(text);
        if (matcher.find()) {
           return matcher.group(1);
        }
        return "{\"Error\":\"Json is invalid\"}";
    }

	public static String converResponseToThinksAsText(String text) {
        Pattern compiledPattern = Pattern.compile("<think>(.+)</think>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(text);
        if (matcher.find()) {
           return matcher.group(1);
        }
        return "No Thinks";
    }
}
