package com.alj.controller;

import com.alj.dto.SessionWebloginDto;
import com.alj.enums.ArticleStatusEnums;
import com.alj.enums.MessageTypeEnum;
import com.alj.enums.ResponseCodeEnum;
import com.alj.enums.UserIntegralOpertype;
import com.alj.exception.BusinessException;
import com.alj.mapper.*;
import com.alj.pojo.*;
import com.alj.service.ForumArticleService;
import com.alj.service.UserInfoService;
import com.alj.service.UserMessageService;
import com.alj.util.CopyTools;
import com.alj.vo.PaginationResultVO;
import com.alj.vo.ResponseVO;
import com.alj.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("userCenterController")
@RequestMapping("/ucenter")
public class UserCenterController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ForumArticleMapper forumArticleMapper;

    @Resource
    private LikeRecordMapper likeRecordMapper;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private UserIntegralRecordMapper userIntegralRecordMapper;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private UserMessageMapper userMessageMapper;
    /**
     * @description: 获取个人信息面板
     * @author alj
     * @date 2024/4/12 16:58
     * @version 1.0
     */
    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(String userId){
        UserInfo userInfo=this.userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("user_id",userId));
        if (userInfo==null||userInfo.getStatus().equals(0)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("status", ArticleStatusEnums.AUDIT.getStatus());
        Integer count=this.forumArticleMapper.selectCount(queryWrapper);
        UserInfoVO userInfoVO= CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setPostCount(count);
        QueryWrapper<LikeRecord> like=new QueryWrapper<>();
        like.eq("user_id",userId);
        Integer likeCount= this.likeRecordMapper.selectCount(like);
        userInfoVO.setLikeCount(likeCount);
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * @description: 获取个人中心的评论和文章
     * @author alj
     * @date 2024/4/13 19:42
     * @version 1.0
     */
    @RequestMapping("/loadUserArticle")
    public ResponseVO loadUserArticle(HttpSession session,String userId,Integer type,Integer  pageNo){
        UserInfo userInfo=this.userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("user_id",userId));
        if (userInfo==null||userInfo.getStatus().equals(0)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("post_time");
        IPage<ForumArticle> pageResult=null;
        if (type==0){
            queryWrapper.eq("user_id",userId);
            SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
            if (webloginDto==null||!webloginDto.getUserId().equals(userId)){
                queryWrapper.eq("status",ArticleStatusEnums.AUDIT);
            }
            Page<ForumArticle> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
            pageResult=forumArticleMapper.selectPage(pageinfo,queryWrapper);
        }else if (type==1){
            //查询本人的评论文章
            pageResult=this.forumArticleService.getArticlesByUserComments(userId,pageNo,5);
        }else if (type==2){
            //查询本人点赞的文章
            pageResult = this.forumArticleService.getUserlike(userId,pageNo,5);
        }
        PaginationResultVO<ForumArticle> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }



    /**
     * @description: 更新个人信息
     * @author alj
     * @date 2024/4/14 10:46
     * @version 1.0
     */
    @RequestMapping("/updateUserInfo")
    public ResponseVO updateUserInfo(HttpSession session, Boolean sex, String personDecp, MultipartFile avatar,String personality){
        SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(webloginDto.getUserId());
        userInfo.setSex(sex);
        userInfo.setPersonDescription(personDecp);
        userInfo.setPersonality(personality);
        userInfoService.updateUserInfo(userInfo,avatar);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 获取积分记录
     * @author alj
     * @date 2024/4/14 13:20
     * @version 1.0
     */
    @RequestMapping("/loadUserIntegralRecord")
    public ResponseVO loadUserIntegralRecord(HttpSession session,String createTimeStart,String createTimeEnd,Integer pageNo){
        SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        IPage<UserIntegralRecord> pageResult=null;
        QueryWrapper<UserIntegralRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",webloginDto.getUserId());
        if (createTimeStart!=null){
            queryWrapper.ge("create_time",createTimeStart);
        }
        if (createTimeEnd!=null){
            queryWrapper.le("create_time",createTimeEnd);
        }
        Page<UserIntegralRecord> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        pageResult=userIntegralRecordMapper.selectPage(pageinfo,queryWrapper);
        PaginationResultVO<UserIntegralRecord> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        resultVO.getList().forEach(record -> {
            UserIntegralOpertype operType = UserIntegralOpertype.getBytype(record.getOperType());
            if (operType != null) {
                record.setOperTypeName(operType.getDesc());
            }
         });
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 获取消息数
     * @author alj
     * @date 2024/4/14 14:34
     * @version 1.0
     */
    @RequestMapping("/getMessageCount")
    public ResponseVO getMessageCount(HttpSession session){
        SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        return getSuccessResponseVO(userMessageService.getUserMessageCount(webloginDto.getUserId()));
    }

    /**
     * @description: 获取消息列表
     * @author alj
     * @date 2024/4/14 16:01
     * @version 1.0
     */
    @RequestMapping("/loadMessageList")
    public ResponseVO loadMessageList(HttpSession session,String code,Integer pageNo){
        MessageTypeEnum typeEnum= MessageTypeEnum.getByCode(code);
        if (typeEnum==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        QueryWrapper<UserMessage> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("received_user_id",sessionWebloginDto.getUserId());
        queryWrapper.eq("message_type",typeEnum.getType());
        queryWrapper.orderByDesc("message_id");
        if (pageNo==null||pageNo==1){
            userMessageService.readMessageByType(sessionWebloginDto.getUserId(),typeEnum.getType());
        }
        Page<UserMessage> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<UserMessage> pageResult=userMessageMapper.selectPage(pageinfo,queryWrapper);
        PaginationResultVO<UserMessage> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }
}
