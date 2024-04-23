package com.alj.config;

import com.alj.Interceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description: 拦截器配置类
 * @author alj
 * @date 2024/4/22 9:13
 * @version 1.0
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addViewController("/toIndexPage").setViewName("/index");
        registry.addViewController("/").setViewName("/index");
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        //注册Interceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(new Interceptor());
        registration.addPathPatterns("/**"); //所有路径都被拦截
        registration.excludePathPatterns(
                "/assets/**"// Vue构建后的静态资源
        );
        registration.excludePathPatterns(
                "/login",       // 登录请求
                "/register",     // 注册请求
                "/getUserInfo",
                "/board/loadBoard",
                "/getSysSetting",
                "/forum/loadArticle",
                "/file/getImage/*",
                "/file/getAvatar/*"
        );
    }

}
