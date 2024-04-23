package com.alj.service.impl;

import com.alj.config.WebConfig;
import com.alj.exception.BusinessException;
import com.alj.mapper.UserInfoMapper;
import com.alj.pojo.EmailCode;
import com.alj.mapper.EmailCodeMapper;
import com.alj.pojo.UserInfo;
import com.alj.service.EmailCodeService;
import com.alj.util.StringTools;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * <p>
 * 邮箱验证码 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class EmailCodeServiceImpl extends ServiceImpl<EmailCodeMapper, EmailCode> implements EmailCodeService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private EmailCodeMapper emailCodeMapper;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private WebConfig webConfig;

    /**
     * @description: 验证邮箱是否存在并加入数据库
     * @author alj
     * @date 2024/2/13 13:41
     * @version 1.0
     */
    @Transactional(rollbackFor = Exception.class)  //如果插入失败应该事务回滚
    public void sendEmailCode(String email,Integer type) {

        if (type==0){ //注册
            QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("email",email);
            UserInfo userInfo=userInfoMapper.selectOne(queryWrapper);
            if (userInfo!=null){
                throw new BusinessException("邮箱已存在");
            }
        }
        String code = StringTools.getRandomString(5);
        sendEmailCodeTo(email,code);
        emailCodeMapper.disableEmailCode(email);
        //int a = 1/0;  测试事务回滚
        EmailCode emailCode=new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setStatus(false);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);

    }

    /**
     * @description: 发送邮件
     * @author alj
     * @date 2024/2/13 13:40
     * @version 1.0
     */
    private void sendEmailCodeTo(String toEmail,String code) {
        try {
            MimeMessage mimeMessage= javaMailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(mimeMessage,true);
            //邮件发送人
            helper.setFrom(webConfig.getSendUserEmail());
            //邮件接收人
            helper.setTo(toEmail);
            helper.setSubject("邮箱验证码");
            helper.setText("邮箱验证码为"+code);
            helper.setSentDate(new Date());
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            throw new BusinessException("邮件发送失败");
        }

    }
    @Override
    public void checkCode(String email,String checkCode){
        QueryWrapper<EmailCode> emailCodeQueryWrapper=new QueryWrapper<>();
        emailCodeQueryWrapper.eq("email",email);
        emailCodeQueryWrapper.eq("code",checkCode);
        EmailCode dbinfo=this.emailCodeMapper.selectOne(emailCodeQueryWrapper);
        if (dbinfo!=null){
            if (dbinfo.getStatus()==false&&System.currentTimeMillis()-dbinfo.getCreateTime().getTime()<=1000*60*15){
                UpdateWrapper<EmailCode> emailCodeUpdateWrapper=new UpdateWrapper<>();
                emailCodeUpdateWrapper.eq("email",email).eq("code",checkCode).set("status",true);
            }else{
                throw new BusinessException("验证码已失效");
            }
        }else {
            throw new BusinessException("邮箱验证码不正确");
        }
    }
}
