package com.alj;

import com.alj.service.SysSettingService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class initRun implements ApplicationRunner {
    @Resource
    private SysSettingService service;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.refreshCache();
    }

}
