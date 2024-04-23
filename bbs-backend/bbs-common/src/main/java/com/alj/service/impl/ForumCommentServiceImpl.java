package com.alj.service.impl;

import com.alj.dto.FileUploadDto;
import com.alj.dto.SessionWebloginDto;
import com.alj.enums.*;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumArticleMapper;
import com.alj.mapper.UserInfoMapper;
import com.alj.mapper.UserMessageMapper;
import com.alj.pojo.ForumArticle;
import com.alj.pojo.ForumComment;
import com.alj.mapper.ForumCommentMapper;
import com.alj.pojo.UserInfo;
import com.alj.pojo.UserMessage;
import com.alj.service.ForumCommentService;
import com.alj.service.UserInfoService;
import com.alj.util.FileUtil;
import com.alj.util.StringTools;
import com.alj.util.SysCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 评论 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentMapper, ForumComment> implements ForumCommentService {
    @Resource
    private ForumCommentMapper forumCommentMapper;

    @Resource
    private ForumArticleMapper forumArticleMapper;


    @Resource
    private UserInfoMapper userInfoMapper;

    @Lazy
    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserMessageMapper userMessageMapper;

    @Resource
    private FileUtil fileUtil;
    /**
     * @description: 查询二级评论hu
     * @author alj
     * @date 2024/4/8 16:33
     * @version 1.0
     */
    @Override
    public void selectChildren(List<ForumComment> primaryComments) {
        for (ForumComment primaryComment : primaryComments) {
            List<ForumComment> secondaryComments = forumCommentMapper.selectList(
                    new QueryWrapper<ForumComment>()
                            .eq("p_comment_id", primaryComment.getCommentId())
                            .eq("status", 1)
                            .eq("article_id",primaryComment.getArticleId())
                            .eq("user_id",primaryComment.getUserId())
                            .orderByAsc("post_time"));
            primaryComment.setSecondaryComments(secondaryComments);
        }
    }

    @Override
    public IPage<ForumComment> selectpage(HttpSession session, String articleId, Integer pageNo, Integer orderType) {
        final String ORDER_TYPE0 = "good_count desc,comment_id asc";
        final String ORDER_TYPE1 = "comment_id desc";
        String orderBy=orderType==null||orderType==0?ORDER_TYPE0:ORDER_TYPE1;
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        QueryWrapper<ForumComment> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId).eq("status", 1).eq("user_id",sessionWebloginDto.getUserId()).eq("p_comment_id",0);
        Page<ForumComment> page=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        page.addOrder(OrderItem.desc("top_type"));
        // 然后根据orderBy变量决定的排序规则添加更多排序条件
        if (ORDER_TYPE0.equals(orderBy)) {
            // 如果是ORDER_TYPE0，接着按good_count降序，comment_id升序排序
            page.addOrder(OrderItem.desc("good_count"));
            page.addOrder(OrderItem.asc("comment_id"));
        } else if (ORDER_TYPE1.equals(orderBy)) {
            // 如果是ORDER_TYPE1，接着按comment_id降序排序
            page.addOrder(OrderItem.desc("comment_id"));
        }
        IPage<ForumComment> ip=forumCommentMapper.selectPage(page,queryWrapper);
        //为每个顶级评论查询二级评论
        if (ip.getRecords() != null && !ip.getRecords().isEmpty()) {
            selectChildren(ip.getRecords());
        }
        return ip;
    }


    /**
     * @description: 设置置顶
     * @author alj
     * @date 2024/4/9 15:57
     * @version 1.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeTopType(String userId, String commentId, Integer topType) {
        CommentTopEnums topEnum=CommentTopEnums.getByType(topType);
        if (null==topEnum){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ForumComment forumComment=forumCommentMapper.selectById(Integer.parseInt(commentId));
        if (null==forumComment){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ForumArticle forumArticle = forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",forumComment.getArticleId()));
        if (forumArticle==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!forumArticle.getUserId().equals(userId)||forumComment.getPCommentId()!=0){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (forumComment.getTopType().equals(topType)){
            return;
        }
        if (CommentTopEnums.TOP.getType().equals(topType)){
            //如果其他评论有置顶，把其他置顶都取消，只保留该评论
            UpdateWrapper<ForumComment> forumCommentUpdateWrapper=new UpdateWrapper<>();
            forumCommentUpdateWrapper.eq("article_id",forumArticle.getArticleId()).eq("top_type",1);
            ForumComment forumComment1=new ForumComment();
            forumComment1.setTopType(0);
            forumCommentMapper.update(forumComment1,forumCommentUpdateWrapper);
        }
        UpdateWrapper<ForumComment> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("comment_id",Integer.parseInt(commentId));
        ForumComment update=new ForumComment();
        update.setTopType(topType);
        forumCommentMapper.update(update,updateWrapper);

    }



    /**
     * @description: 添加评论
     * @author alj
     * @date 2024/4/11 11:17
     * @version 1.0
     */
    @Override
    public void postComment(ForumComment comment, MultipartFile image) {
        ForumArticle forumArticle=forumArticleMapper.selectById(comment.getArticleId());
        //如果文章为空或者未审核状态
        if (forumArticle==null||!forumArticle.getStatus().equals(1)){
            throw new BusinessException("评论的文章不存在");
        }
        ForumComment pComment=null;

        if (comment.getPCommentId()!=0){
            QueryWrapper<ForumComment> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("commnet_id",comment.getPCommentId());
            pComment=forumCommentMapper.selectOne(queryWrapper);
            if (pComment==null){
                throw new BusinessException("回复的评论不存在");
            }
        }

        if (!StringTools.isEmpty(comment.getReplyNickName())){
            UserInfo userInfo=userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("user_id",comment.getReplyUserId()));
            if (userInfo==null){
                throw new BusinessException("回复的用户不存在");
            }
            comment.setReplyNickName(userInfo.getNickName());
        }
        comment.setPostTime(new Date());
        if (image!=null){
            //上传图片
            FileUploadDto fileUploadDto=fileUtil.uploadFile2local(image,"images/",FileUpLoadTypeEnum.COMMENT_IMAGE);
            comment.setImgPath(fileUploadDto.getLocalPath());
        }

        //评论是否需要审核
        Boolean needAudit = SysCacheUtil.getSysSetting().getSysSettingAudit().getCommentAudit();
        comment.setStatus(needAudit?CommentStatusEnum.NO_AUDIT.getStatus():CommentStatusEnum.AUDIT.getStatus());
        this.forumCommentMapper.insert(comment);
        if(needAudit){
            return;
        }
        updateCommentInfo(comment,forumArticle,pComment);

    }


    /**
     * @description: 更新评论相关信息，包括积分，评论数和消息
     * @author alj
     * @date 2024/4/11 12:27
     * @version 1.0
     */
    public void updateCommentInfo(ForumComment comment,ForumArticle article,ForumComment pComment){
        Integer commentInteger = SysCacheUtil.getSysSetting().getSysSettingComment().getCommentIntegral();
        //增加积分
        if (commentInteger>0){
            UserInfo userInfo=this.userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("user_id",comment.getUserId()));
            UpdateWrapper<UserInfo> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("user_id",comment.getUserId());
            UserInfo update = new UserInfo();
            update.setCurrentIntegral(userInfo.getCurrentIntegral()+commentInteger);
            this.userInfoMapper.update(update,updateWrapper);
        }
        //增加评论数
        if (comment.getPCommentId()==0){
            ForumArticle forumArticle=forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",comment.getArticleId()));
            UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("article_id",comment.getArticleId());
            ForumArticle update=new ForumArticle();
            update.setCommentCount(forumArticle.getCommentCount()+1);
            this.forumArticleMapper.update(update,updateWrapper);
        }
        //记录消息
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageType(MessageTypeEnum.COMMENT.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setArticleId(comment.getArticleId());
        userMessage.setCommentId(comment.getCommentId());
        userMessage.setSendUserId(comment.getUserId());
        userMessage.setSendNickName(comment.getNickName());
        userMessage.setStatus(MessageStatus.NO_READ.getStatus());
        userMessage.setArticleTitle(article.getTitle());
        if (comment.getPCommentId()==0){
            userMessage.setReceivedUserId(article.getUserId());
        }else if(comment.getPCommentId()!=0&&StringTools.isEmpty(comment.getReplyUserId())){
            userMessage.setReceivedUserId(pComment.getUserId());
        }else if (comment.getPCommentId()!=0&&!StringTools.isEmpty(comment.getReplyUserId())){
            userMessage.setReceivedUserId(comment.getReplyUserId());
        }
        if (!comment.getUserId().equals(userMessage.getReceivedUserId())){
            this.userMessageMapper.insert(userMessage);
        }
    }

    /**
     * @description: 管理员删除评论
     * @author alj
     * @date 2024/4/15 16:08
     * @version 1.0
     */
    @Override
    public void delComment(String commentIds) {
        String[] commentIdsArray=commentIds.split(",");
        for (String commentId:commentIdsArray){
            Integer comment=Integer.parseInt(commentId);
            forumCommentService.delCommentSingle(comment);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delCommentSingle(Integer commentId) {
        ForumComment forumComment=forumCommentMapper.selectOne(new QueryWrapper<ForumComment>().eq("comment_id",commentId));
        if (null==forumComment||!CommentStatusEnum.AUDIT.getStatus().equals(forumComment.getStatus())){
            return;
        }
        ForumComment update=new ForumComment();
        update.setStatus(CommentStatusEnum.DEL.getStatus());
        forumCommentMapper.update(update,new UpdateWrapper<ForumComment>().eq("comment_id",commentId));
        if(forumComment.getStatus().equals(CommentStatusEnum.AUDIT.getStatus())){
            if (forumComment.getPCommentId()==0){
                //更新文章评论数量
                ForumArticle forumArticle=forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",forumComment.getArticleId()));
                UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
                updateWrapper.eq("article_id",forumArticle.getArticleId());
                ForumArticle updateArticle=new ForumArticle();
                updateArticle.setCommentCount(forumArticle.getCommentCount()-1);
                this.forumArticleMapper.update(updateArticle,updateWrapper);
            }
            //更新个人积分
            Integer integral=SysCacheUtil.getSysSetting().getSysSettingComment().getCommentIntegral();
            userInfoService.updateUserIntegral(forumComment.getUserId(),UserIntegralOpertype.DEL_COMMENT,-1,integral);
        }
        UserMessage userMessage=new UserMessage();
        userMessage.setReceivedUserId(forumComment.getUserId());
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setMessageContent("您的评论【"+forumComment.getContent()+"】被删除啦！");
        userMessage.setCreateTime(new Date());
        userMessageMapper.insert(userMessage);
    }

    @Override
    public void auditComment(String commentIds) {
        String[] commentIdsArray=commentIds.split(",");
        for (String commentIdstr:commentIdsArray){
            Integer commentId=Integer.parseInt(commentIdstr);
            forumCommentService.auditCommentSingle(commentId);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCommentSingle(Integer commentId) {
        ForumComment comment=forumCommentMapper.selectOne(new QueryWrapper<ForumComment>().eq("comment_id",commentId));
        if (comment==null||comment.getStatus().equals(CommentStatusEnum.AUDIT.getStatus())){
            return;
        }
        ForumComment update=new ForumComment();
        update.setStatus(CommentStatusEnum.AUDIT.getStatus());
        forumCommentMapper.update(update,new UpdateWrapper<ForumComment>().eq("comment_id",commentId));
        ForumArticle forumArticle=forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",comment.getArticleId()));
        ForumComment pComment=null;
        if (comment.getCommentId()!=0&&StringTools.isEmpty(comment.getReplyUserId())){
            pComment=forumCommentMapper.selectOne(new QueryWrapper<ForumComment>().eq("p_comment_id",comment.getPCommentId()));

        }
        updateCommentInfo(comment,forumArticle,pComment);
    }
}
