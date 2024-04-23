package com.alj.service.impl;

import com.alj.config.WebConfig;
import com.alj.dto.SessionWebloginDto;
import com.alj.enums.*;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumArticleMapper;
import com.alj.mapper.UserIntegralRecordMapper;
import com.alj.mapper.UserMessageMapper;
import com.alj.pojo.UserInfo;
import com.alj.mapper.UserInfoMapper;
import com.alj.pojo.UserIntegralRecord;
import com.alj.pojo.UserMessage;
import com.alj.service.EmailCodeService;
import com.alj.service.UserInfoService;
import com.alj.util.FileUtil;
import com.alj.util.StringTools;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.catalina.User;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Date;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private EmailCodeService emailCodeService;
    @Resource
    private UserMessageMapper userMessageMapper;
    @Resource
    private UserIntegralRecordMapper userIntegralRecordMapper;
    @Resource
    private WebConfig webConfig;

    @Resource
    private ForumArticleMapper forumArticleMapper;

    @Resource
    private FileUtil fileUtil;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void Register(String email, String nickName, String password,String emailCode,String personality) throws MessagingException {
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        UserInfo userInfo = this.userInfoMapper.selectOne(queryWrapper);
        if (userInfo==null){
            queryWrapper.eq("nick_name",nickName);
            userInfo=this.userInfoMapper.selectOne(queryWrapper);
            if (userInfo==null){
                emailCodeService.checkCode(email,emailCode);
            }else{
                throw new BusinessException("昵称已存在");
            }
        }else{
            throw new BusinessException("邮箱号已存在");
        }

        String userId= StringTools.getRandomNum(10);
        UserInfo insertinfo=new UserInfo();
        insertinfo.setUserId(userId);
        insertinfo.setNickName(nickName);
        insertinfo.setEmail(email);
        insertinfo.setPassword(password);
        insertinfo.setJoinTime(new Date());
        insertinfo.setStatus(1);
        insertinfo.setPersonality(personality);
        insertinfo.setTotalIntegral(0);
        insertinfo.setCurrentIntegral(0);
        this.userInfoMapper.insert(insertinfo);
        System.out.println(5);
        //更新用户积分,注册成功加5分
        updateUserIntegral(userId,UserIntegralOpertype.REGISTER,1,5);
        //记录消息
        UserMessage userMessage=new UserMessage();
        userMessage.setReceivedUserId(userId);
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageStatus.NO_READ.getStatus());
        userMessage.setMessageContent("欢迎您加入情感论坛！");
        this.userMessageMapper.insert(userMessage);

    }

    @Override
    public SessionWebloginDto login(String email, String password) {
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        UserInfo userInfo=this.userInfoMapper.selectOne(queryWrapper);
        if (null==userInfo||!userInfo.getPassword().equals(password)){
            throw new BusinessException("账号不存在或密码错误");
        }
        if (userInfo.getStatus()==0){
            throw new BusinessException("账号已被禁用");
        }
        UpdateWrapper<UserInfo> userInfoUpdateWrapper=new UpdateWrapper<>();
        userInfoUpdateWrapper.eq("user_id",userInfo.getUserId());
        UserInfo userInfo1=new UserInfo();
        userInfo1.setLastLoginTime(new Date());
        this.userInfoMapper.update(userInfo1,userInfoUpdateWrapper);
        SessionWebloginDto sessionWebloginDto=new SessionWebloginDto();
        sessionWebloginDto.setUserId(userInfo.getUserId());
        sessionWebloginDto.setNickName(userInfo.getNickName());
        if (!StringTools.isEmpty(webConfig.getAdminEmails())&& ArrayUtils.contains(webConfig.getAdminEmails().split(","),userInfo.getEmail())){
            sessionWebloginDto.setAdmin(true);
        }else{
            sessionWebloginDto.setAdmin(false);
        }
        return sessionWebloginDto;
    }

    /**
     * @description: 更新积分
     * @author alj
     * @date 2024/4/3 11:04
     * @version 1.0
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserIntegral(String userId, UserIntegralOpertype userIntegralOpertype,Integer changeType,Integer integral){
        integral=changeType*integral;
        if(integral==0){
            return ;
        }
        UserInfo userInfo=userInfoMapper.selectById(userId);
        if (userInfo.getCurrentIntegral()+integral<0){
            integral=changeType*userInfo.getCurrentIntegral();

        }
        UserIntegralRecord userIntegralRecord=new UserIntegralRecord();
        userIntegralRecord.setUserId(userId);
        userIntegralRecord.setOperType(userIntegralOpertype.getOperType());
        userIntegralRecord.setCreateTime(new Date());
        userIntegralRecord.setIntegral(integral);
        this.userIntegralRecordMapper.insert(userIntegralRecord);
        UpdateWrapper<UserInfo> userInfoUpdateWrapper=new UpdateWrapper<>();
        UserInfo userInfo1=new UserInfo();
        userInfo1.setCurrentIntegral(userInfo.getCurrentIntegral()+integral);
        userInfoUpdateWrapper.eq("user_id",userId);
        this.userInfoMapper.update(userInfo1,userInfoUpdateWrapper);
        if (integral>0){
            userInfo1.setTotalIntegral(userInfo.getTotalIntegral()+integral);
            this.userInfoMapper.update(userInfo1,userInfoUpdateWrapper);
        }
    }

    /**
     * @description: 更新密码
     * @author alj
     * @date 2024/4/4 14:32
     * @version 1.0
     */
    @Override
    public void resetPwd(String email, String password, String emailCode) {
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        UserInfo userInfo=this.userInfoMapper.selectOne(queryWrapper);
        emailCodeService.checkCode(email,emailCode);
        UserInfo updateInfo = new UserInfo();
        updateInfo.setPassword(password);
        UpdateWrapper<UserInfo> userInfoUpdateWrapper=new UpdateWrapper<>();
        userInfoUpdateWrapper.eq("email",email);
        userInfoMapper.update(updateInfo,userInfoUpdateWrapper);

    }

    /**
     * @description: 更新个人信息
     * @author alj
     * @date 2024/4/14 11:00
     * @version 1.0
     */
    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatar) {
        UpdateWrapper<UserInfo> userInfoUpdateWrapper=new UpdateWrapper<>();
        userInfoUpdateWrapper.eq("user_id",userInfo.getUserId());
        userInfoMapper.update(userInfo,userInfoUpdateWrapper);
        if (avatar!=null){
            fileUtil.uploadFile2local(avatar,userInfo.getUserId(), FileUpLoadTypeEnum.AVATAR);
        }
    }

    /**
     * @description: 修改用户状态
     * @author alj
     * @date 2024/4/16 15:30
     * @version 1.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Integer status, String userId) {
        if (status==0){
            //把该作者文章也删除
            forumArticleMapper.updateArticleByUserId(ArticleStatusEnums.DEL.getStatus(), userId);
        }
        UserInfo update=new UserInfo();
        update.setStatus(status);
        UpdateWrapper<UserInfo> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("user_id",userId);
        userInfoMapper.update(update,updateWrapper);
    }

    /**
     * @description: 发送消息
     * @author alj
     * @date 2024/4/16 15:56
     * @version 1.0
     */
    @Override
    public void sendMessage(String userId, String message, Integer integral) {
        UserMessage userMessage=new UserMessage();
        userMessage.setReceivedUserId(userId);
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageTypeEnum.SYS.getType());
        userMessage.setMessageContent(message);
        userMessageMapper.insert(userMessage);
        //更新用户积分
        Integer changeType=1;
        if (integral!=null&&integral!=0){
            if (integral<0){
                integral=integral*-1;
                changeType=-1;
            }
            updateUserIntegral(userId,UserIntegralOpertype.ADMIN,changeType,integral);
        }
    }
}
