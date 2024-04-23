package com.alj;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.stereotype.Component;

@MapperScan("com.alj.mapper")
@SpringBootApplication
public class BbsWebApplication {
    public static void main(String[] args){
        SpringApplication.run(BbsWebApplication.class,args);
    }
}
