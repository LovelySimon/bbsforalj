package com.alj.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("adminConfig")
public class AdminConfig extends  AppConfig{
    @Value("admin")
    private String adminAccount;

    @Value("admin123")
    private String adminPassword;


    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
