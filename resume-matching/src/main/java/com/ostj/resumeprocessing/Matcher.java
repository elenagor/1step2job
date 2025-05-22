package com.ostj.resumeprocessing;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;

import com.azure.ai.openai.models.ChatRequestUserMessage;

import com.azure.core.credential.KeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.HttpClientOptions;

import io.netty.util.internal.StringUtil;

@Configuration
@PropertySource("classpath:application.properties")
public class Matcher {
    private static Logger log = LoggerFactory.getLogger(Matcher.class);

    @Value(value = "${ostj.openai.apikey}")
    String apiKey;

    @Value(value = "${ostj.openai.endpoint}")
    String endpoint;

    @Value(value = "${ostj.openai.model}")
    String model;

    public String run_resume_matching(String resumeFilePath, String jdFilePath, String promptFilePath) throws Exception{
        System.out.println("Matcher: apiKey="+apiKey+",endpoint="+endpoint+",model="+model);
        System.out.println("Matcher: resumeFilePath="+resumeFilePath+",jdFilePath="+jdFilePath+",promptFilePath="+promptFilePath);

        String resume = readFile(resumeFilePath);
        String job_description = readFile(jdFilePath);
        String prompt = readFile(promptFilePath);

        log.trace("Matcher: resume="+resume);
        log.trace("Matcher: job_description="+job_description);
        log.trace("Matcher: prompt="+prompt);

        return call_openai( resume,  job_description,  prompt);
    }

    private static String readFile(String path) throws Exception{
        if( StringUtil.isNullOrEmpty(path)){
            return "";
        }
        FileInputStream fis = new FileInputStream(path);
        return IOUtils.toString(fis, "UTF-8");
    }

    public String call_openai( String resume, String job_description, String prompt) throws Exception {
        String user_content = "";
        if(StringUtil.isNullOrEmpty(job_description)){
            // Replace {resume} placeholder
            user_content = prompt.replace("{resume}", resume);
        }
        else{
            user_content = prompt.replace("{resume}", resume);
            user_content = prompt.replace("{job_description}", job_description);
        }
        
        log.trace("Matcher: user message="+user_content);

        //ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8000));
        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                //.clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
                .credential(new KeyCredential(apiKey))
                .buildClient();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();
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
