package com.alj.controller;

import com.alj.dto.SessionWebloginDto;
import com.alj.enums.CommentTopEnums;
import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumCommentMapper;
import com.alj.pojo.ForumComment;
import com.alj.pojo.LikeRecord;
import com.alj.service.ForumCommentService;
import com.alj.service.LikeRecordService;
import com.alj.util.StringTools;
import com.alj.util.SysCacheUtil;
import com.alj.vo.PaginationResultVO;
import com.alj.vo.ResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @description: 管理评论controller
 * @author alj
 * @date 2024/4/8 14:25
 * @version 1.0
 */
@RestController
@RequestMapping("/comment")
public class ForumCommentController extends ABaseController{
    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private ForumCommentMapper forumCommentMapper;

    /**
     * @description: 展示评论列表
     * @author alj
     * @date 2024/4/8 14:26
     * @version 1.0
     */
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(HttpSession session,String articleId,Integer pageNo,Integer orderType){
        //未打开评论
        if (!SysCacheUtil.getSysSetting().getSysSettingComment().getCommentOpen()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        IPage<ForumComment> pageResult = forumCommentService.selectpage(session,articleId,pageNo,orderType);
        // 创建PaginationResultVO实例
        PaginationResultVO<ForumComment> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/doLikeComment")
    public ResponseVO doLikeComment(HttpSession session,Integer commentId){
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        String objectId=String.valueOf(commentId);
        likeRecordService.dolike(objectId,sessionWebloginDto.getUserId(),sessionWebloginDto.getNickName(),1);
        LikeRecord record=likeRecordService.getLikeRecordByOUT(objectId,sessionWebloginDto.getUserId(),1);
        ForumComment forumComment=forumCommentService.getById(commentId);
        forumComment.setLikeType(record==null?false:true);
        return getSuccessResponseVO(forumComment);
    }

    @RequestMapping("/changeTopType")
    public ResponseVO changeTopType(HttpSession session,Integer commentId,Integer topType){
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        forumCommentService.changeTopType(sessionWebloginDto.getUserId(),String.valueOf(commentId),topType);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/postComment")
    public ResponseVO postComment(HttpSession session, String articleId, String pCommentId, String content, MultipartFile image,String replyUserId){
        if (!SysCacheUtil.getSysSetting().getSysSettingComment().getCommentOpen()){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (image==null&& StringTools.isEmpty(content)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        content=StringTools.convertHTML(content);
        ForumComment comment=new ForumComment();
        comment.setUserId(sessionWebloginDto.getUserId());
        comment.setNickName(sessionWebloginDto.getNickName());
        comment.setPCommentId(Integer.parseInt(pCommentId));
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setReplyUserId(replyUserId);
        comment.setTopType(CommentTopEnums.NO_TOP.getType());
        forumCommentService.postComment(comment,image);
        if (Integer.parseInt(pCommentId)!=0){
            QueryWrapper<ForumComment> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("article_id",articleId);
            queryWrapper.eq("p_comment_id",pCommentId);
            queryWrapper.orderByAsc("comment_id");
            List<ForumComment> children = forumCommentMapper.selectList(queryWrapper);
            return getSuccessResponseVO(children);
        }
        return getSuccessResponseVO(comment);
    }
}
