 package com.alj.controller;

import com.alj.config.AdminConfig;
import com.alj.dto.SessionAdminloginDto;
import com.alj.dto.SessionWebloginDto;
import com.alj.dto.SysSettingComment;
import com.alj.dto.SysSettingDto;
import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import com.alj.service.EmailCodeService;
import com.alj.service.UserInfoService;
import com.alj.util.CreateImageCode;
import com.alj.util.StringTools;
import com.alj.util.SysCacheUtil;
import com.alj.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

 @RestController
public class AccountController extends ABaseController {
//    验证码
        @Resource
        private AdminConfig adminConfig;

    /**
     * @description: 验证码图片生成
     * @author alj
     * @date 2024/2/14 10:33
     * @version 1.0
     */
    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session) throws IOException {
        CreateImageCode createImageCode=new CreateImageCode(130,38,5,10);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code=createImageCode.getCode();
        //登录注册的验证码
        session.setAttribute("check_code",code);
        createImageCode.write(response.getOutputStream());
    }



    @RequestMapping("/login")
    public ResponseVO login(HttpSession session,String account,String password,String checkCode){
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code")) ) {
                throw new BusinessException("图片验证码错误");
            }
            if (!adminConfig.getAdminAccount().equals(account)||!adminConfig.getAdminPassword().equals(password)){
                throw new BusinessException("账号或者密码错误");
            }
            SessionAdminloginDto sessionAdminloginDto= new SessionAdminloginDto();
            sessionAdminloginDto.setAccount(account);
            session.setAttribute("session_key",sessionAdminloginDto);
            return getSuccessResponseVO(sessionAdminloginDto);
        }finally {
            session.removeAttribute("check_code");
        }


    }



    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting(){
        SysSettingDto sysSettingDto= SysCacheUtil.getSysSetting();
        SysSettingComment sysSettingComment=sysSettingDto.getSysSettingComment();
        Map<String,Object> result=new HashMap<>();
        result.put("commentOpen",sysSettingComment.getCommentOpen());
        return getSuccessResponseVO(result);
    }


 }
