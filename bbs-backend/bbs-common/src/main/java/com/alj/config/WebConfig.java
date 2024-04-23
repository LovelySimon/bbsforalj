package com.alj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebConfig extends AppConfig{
    @Value("${spring.mail.username}")
    private String sendUserEmail;
    @Value("${admin.emails:}")
    private String adminEmails;

    public String getAdminEmails() {
        return adminEmails;
    }

    public String getSendUserEmail() {
        return sendUserEmail;
    }
}
