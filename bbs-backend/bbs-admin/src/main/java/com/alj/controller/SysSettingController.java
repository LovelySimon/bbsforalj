package com.alj.controller;

import com.alj.config.AdminConfig;
import com.alj.dto.*;
import com.alj.pojo.SysSetting;
import com.alj.service.SysSettingService;
import com.alj.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting")
public class SysSettingController extends ABaseController{
    @Resource
    private SysSettingService sysSettingService;

    @Resource
    private AdminConfig adminConfig;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting(){
        return getSuccessResponseVO(sysSettingService.refreshCache());
    }

    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(Boolean commentAudit,
                                  Boolean postAudit,
                                  Integer commentDayCountThreshold,
                                  Integer commentIntegral,
                                  Boolean commentOpen,
                                  Integer likeCount,
                                  Integer postIntegral,
                                  Integer postDayCountThreshold,
                                  Integer dayImageUploadCount,
                                  String registerWelcomInfo,
                                  String emailTitle,
                                  String emailContent,
                                  Integer likeDayCountThreshold
    ){
        SysSettingAudit sysSettingAudit=new SysSettingAudit();
        sysSettingAudit.setCommentAudit(commentAudit);
        sysSettingAudit.setPostAudit(postAudit);
        SysSettingComment sysSettingComment=new SysSettingComment();
        sysSettingComment.setCommentIntegral(commentIntegral);
        sysSettingComment.setCommentOpen(commentOpen);
        sysSettingComment.setCommentDayCountThreshold(commentDayCountThreshold);
        SysSettingLike sysSettingLike=new SysSettingLike();
        sysSettingLike.setLikeDayCountThreshold(likeDayCountThreshold);
        SysSettingPost sysSettingPost=new SysSettingPost();
        sysSettingPost.setPostIntegral(postIntegral);
        sysSettingPost.setPostDayCountThreshold(postDayCountThreshold);
        sysSettingPost.setDayImageUploadCount(dayImageUploadCount);
        SysSettingRegister sysSettingRegister=new SysSettingRegister();
        sysSettingRegister.setRegisterWelcomInfo(registerWelcomInfo);
        SysSettingsEmail sysSettingsEmail=new SysSettingsEmail();
        sysSettingsEmail.setEmailContent(emailContent);
        sysSettingsEmail.setEmailTitle(emailTitle);
        //将sys装入sysdto
        SysSettingDto sysSettingDto=new SysSettingDto();
        sysSettingDto.setSysSettingAudit(sysSettingAudit);
        sysSettingDto.setSysSettingLike(sysSettingLike);
        sysSettingDto.setSysSettingComment(sysSettingComment);
        sysSettingDto.setSysSettingPost(sysSettingPost);
        sysSettingDto.setSysSettingRegister(sysSettingRegister);
        sysSettingDto.setSysSettingsEmail(sysSettingsEmail);

        this.sysSettingService.saveSetting(sysSettingDto);

        return getSuccessResponseVO(null);
    }
}
