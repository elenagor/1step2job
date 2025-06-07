package com.ostj.resumeprocessing;

import org.junit.jupiter.api.Test;

import com.ostj.utils.EmailSender;

public class EmailSenderTest {
    
    @Test
    void testEmailSend() throws Exception{
        EmailSender emailSender = EmailSender.getEmailSender( "mail.privateemail.com",  "support@1step2job.ai",  "0uc@H88PpxBofVWx");
        emailSender.setPort("587");
        emailSender.setUseSSL("true");
        emailSender.withTO("elena.gordienko@1step2job.ai")
                    .withBody("")
                    .withSubject("1Step2Job found a job for you")
                    .send("noreply@1step2job.ai");  
    }
}
