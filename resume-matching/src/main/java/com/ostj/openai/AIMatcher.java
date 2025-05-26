package com.ostj.openai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.services.blocking.VectorStoreService;

import kotlin.text.Charsets;

public class AIMatcher {
	private static Logger log = LoggerFactory.getLogger(AIMatcher.class);

	String apiKey;
	String endpoint;
	String model;

	public AIMatcher(String apiKey, String endpoint, String model) {
		super();
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.model = model;
	}

	public String call_openai(String resume, String job_description, String prompt) throws Exception {
		String user_content = "";
		if (StringUtils.isEmpty(job_description)) {
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
		return getJsonContextAsString( chatCompletion.choices().get(0).message() );
	}

	private String getJsonContextAsString(ChatCompletionMessage  message){
		Optional<String> content = message.content();
		return getJsonContextAsString(content.get() );
	}

	private String getJsonContextAsString(String text) {
        Pattern compiledPattern = Pattern.compile("^[^{]*(\\{.*\\})[^}]*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(text);
        if (matcher.find()) {
           return matcher.group(1);
        }
        return null;
    }

}
