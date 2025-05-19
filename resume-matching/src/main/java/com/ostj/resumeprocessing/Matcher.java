package com.ostj.resumeprocessing;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.KeyCredential;

@Configuration
@PropertySource("classpath:application.properties")
public class Matcher {
    @Value(value = "${ostj.openai.apikey}")
    String apiKey;

    @Value(value = "${ostj.openai.endpoint}")
    String endpoint;

    @Value(value = "${ostj.openai.model}")
    String model;

    public Matcher(){
        System.out.println("apiKey="+apiKey+",endpoint="+endpoint+",model="+model+"\n");
    }

    public String run_resume_matching(String resumeFilePath, String jdFilePath, String promptFilePath) throws Exception{
        String resume = readFile(resumeFilePath);
        String job_description = readFile(jdFilePath);
        String prompt = readFile(promptFilePath);

        return call_openai( resume,  job_description,  prompt);
    }

    private static String readFile(String path) throws Exception{
        FileInputStream fis = new FileInputStream(path);
        return IOUtils.toString(fis, "UTF-8");
    }

    public String call_openai( String resume, String job_description, String prompt) throws Exception {
        String user_content = String.format("Resume:%s\nJob Description:%s", resume, job_description);

        System.out.println("apiKey="+apiKey+",endpoint="+endpoint+",model="+model+"\n");

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatRequestSystemMessage(prompt));
        chatMessages.add(new ChatRequestUserMessage(user_content));
        
        ChatCompletions chatCompletions = client.getChatCompletions(model, new ChatCompletionsOptions(chatMessages));

        StringBuffer sb = new StringBuffer();
        for (ChatChoice choice : chatCompletions.getChoices()) {
            sb.append(choice.getMessage().getContent());
        }

        //CompletionsUsage usage = chatCompletions.getUsage();
        //System.out.printf("Usage: number of prompt token is %d, "
        //                + "number of completion token is %d, and number of total tokens in request and response is %d.%n",
        //        usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
 
        return sb.toString();
    }
}
