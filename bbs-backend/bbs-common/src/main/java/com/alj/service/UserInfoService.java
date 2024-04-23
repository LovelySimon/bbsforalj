package com.alj.service;

import com.alj.dto.SessionWebloginDto;
import com.alj.enums.UserIntegralOpertype;
import com.alj.pojo.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface UserInfoService extends IService<UserInfo> {
 void Register(String email,String nickName,String password,String emailCode,String personality) throws MessagingException;

 SessionWebloginDto login(String email,String password);

  void updateUserIntegral(String userId, UserIntegralOpertype userIntegralOpertype, Integer changeType, Integer integral);
 void resetPwd(String email,String password,String emailCode);

 void updateUserInfo(UserInfo userInfo, MultipartFile avatar);

 void updateUserStatus(Integer status, String userId);

 void sendMessage(String userId, String message, Integer integral);
}
