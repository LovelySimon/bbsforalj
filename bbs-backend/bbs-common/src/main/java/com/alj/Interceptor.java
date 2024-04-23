package com.alj;

import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: 登录拦截器
 * @author alj
 * @date 2024/4/22 9:00
 * @version 1.0
 */
public class Interceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String uri = request.getRequestURI();
        //判断当前请求地址是否登录地址
        if(uri.contains("login") || uri.contains("register")||uri.contains("sendEmailCode")||uri.contains("checkCode"))
        {
            //登录请求，直接放行
            return true;
        }
        else {
            //判断用户是否登录
            if (request.getSession().getAttribute("session_key") != null) {
                //说明已经登录，放行
                return true;
            } else {
                //没有登录，重定向到登录界面
                throw new BusinessException(ResponseCodeEnum.CODE_901);
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
