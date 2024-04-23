package com.alj;

import com.alj.mapper.UserInfoMapper;
import com.alj.pojo.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
@MapperScan(basePackages = {"com.alj.mapper"})
@SpringBootApplication
public class BbsAdminApplication {

    public static void main(String[] args){
        SpringApplication.run(BbsAdminApplication.class,args);
    }

}
