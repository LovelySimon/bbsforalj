 package com.alj.controller;
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
    private EmailCodeService emailCodeService;
    @Resource
    private UserInfoService userInfoService;

    /**
     * @description: 验证码图片生成
     * @author alj
     * @date 2024/2/14 10:33
     * @version 1.0
     */
    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        CreateImageCode createImageCode=new CreateImageCode(130,38,5,10);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code=createImageCode.getCode();
        if (type==null||type==0) {
            //登录注册的验证码
            session.setAttribute("check_code",code);
        }else{
            //邮箱验证码
            session.setAttribute("check_code_email",code);
        }
        createImageCode.write(response.getOutputStream());
    }

    /**
     * @description: 发送邮箱验证码
     * @author alj
     * @date 2024/2/15 13:18
     * @version 1.0
     */
    @RequestMapping("/sendEmailCode")
    public ResponseVO sendEmailCode(HttpSession session,String email,String checkCode,Integer type) throws MessagingException {
       try{
           if (StringTools.isEmpty(email)||StringTools.isEmpty(checkCode)||type==null){
           throw new BusinessException(ResponseCodeEnum.CODE_600);
                }
           if (!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code_email")) ) {
               throw new BusinessException("图片验证码错误");
           }
           emailCodeService.sendEmailCode(email,type);
           return  getSuccessResponseVO(null);
       }
       finally {
           session.removeAttribute("check_code_email");
       }

    }

    @RequestMapping("/register")
    public ResponseVO register(HttpSession session,String checkCode,String email,String emailCode,String nickName,String password,String personality) throws MessagingException {

   try{
       if (StringTools.isEmpty(checkCode)||StringTools.isEmpty(email)||StringTools.isEmpty(emailCode)||StringTools.isEmpty(password)||StringTools.isEmpty(nickName)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
             }
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code")) ) {
                throw new BusinessException("图片验证码错误");
             }
            userInfoService.Register(email,nickName,password,emailCode,personality);
            return getSuccessResponseVO(null);
   }finally {
       session.removeAttribute("check_code");
   }

    }


    @RequestMapping("/login")
    public ResponseVO login(HttpSession session,String email,String password,String checkCode){
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code")) ) {
                throw new BusinessException("图片验证码错误");
            }
            SessionWebloginDto sessionWebloginDto= userInfoService.login(email,password);
            session.setAttribute("session_key",sessionWebloginDto);
            return getSuccessResponseVO(null);
        }finally {
            session.removeAttribute("check_code");
        }


    }

    /**
     * @description: 获取登录信息
     * @author alj
     * @date 2024/4/4 10:37
     * @version 1.0
     */
    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(HttpSession session){
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        return getSuccessResponseVO(sessionWebloginDto);
    }

     /**
      * @description: 登出账户
      * @author alj
      * @date 2024/4/4 10:37
      * @version 1.0
      */
    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session){
        session.invalidate();
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting(){
        SysSettingDto sysSettingDto= SysCacheUtil.getSysSetting();
        SysSettingComment sysSettingComment=sysSettingDto.getSysSettingComment();
        Map<String,Object> result=new HashMap<>();
        result.put("commentOpen",sysSettingComment.getCommentOpen());
        return getSuccessResponseVO(result);
    }

     @RequestMapping("/resetPwd")
     public ResponseVO resetPwd(HttpSession session,String email,String password,String checkCode,String emailCode){
         try {
             if (!checkCode.equalsIgnoreCase((String) session.getAttribute("check_code")) ) {
                 throw new BusinessException("图片验证码错误");
             }
             userInfoService.resetPwd(email,password,emailCode);
             return getSuccessResponseVO(null);
         }finally {
             session.removeAttribute("check_code");
         }
     }
 }
