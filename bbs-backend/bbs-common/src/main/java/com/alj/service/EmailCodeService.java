package com.alj.service;

import com.alj.pojo.EmailCode;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.mail.MessagingException;

/**
 * <p>
 * 邮箱验证码 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface EmailCodeService extends IService<EmailCode> {
      void sendEmailCode(String email,Integer type) throws MessagingException;

      void checkCode(String email,String checkCode);
}
