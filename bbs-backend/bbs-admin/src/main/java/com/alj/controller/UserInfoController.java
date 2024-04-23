package com.alj.controller;

import com.alj.mapper.UserInfoMapper;
import com.alj.pojo.ForumComment;
import com.alj.pojo.UserInfo;
import com.alj.service.UserInfoService;
import com.alj.util.StringTools;
import com.alj.vo.PaginationResultVO;
import com.alj.vo.ResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserInfoController extends ABaseController{
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserInfoService userInfoService;

    /**
     * @description: 加载用户列表
     * @author alj
     * @date 2024/4/16 15:00
     * @version 1.0
     */
    @RequestMapping("/loadUserList")
    public ResponseVO  loadUserList(Integer pageNo,String nickNameFuzzy,Integer sex,Integer status){
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        if (!StringTools.isEmpty(nickNameFuzzy)){
            queryWrapper.like("nick_name",nickNameFuzzy);
        }
        if (sex!=null){
            queryWrapper.eq("sex",sex);
        }
        if (status!=null){
            queryWrapper.eq("status",status);
        }
        Page<UserInfo> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<UserInfo> pageResult=userInfoMapper.selectPage(pageinfo,queryWrapper);
        PaginationResultVO<UserInfo> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 更新用户状态
     * @author alj
     * @date 2024/4/16 15:40
     * @version 1.0
     */
    @RequestMapping("/updateUserStatus")
    public ResponseVO updateUserStatus(Integer status,String userId){
        userInfoService.updateUserStatus(status,userId);
        return getSuccessResponseVO(null);
    }


    /**
     * @description: 为用户发送消息
     * @author alj
     * @date 2024/4/16 15:39
     * @version 1.0
     */
    @RequestMapping("/sendMessage")
    public ResponseVO sendMessage(String userId,String message,Integer integral){
        userInfoService.sendMessage(userId,message,integral);
        return getSuccessResponseVO(null);
    }
}
