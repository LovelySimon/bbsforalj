package com.alj.service.impl;

import com.alj.dto.FileUploadDto;
import com.alj.dto.SessionWebloginDto;
import com.alj.dto.SysSettingAudit;
import com.alj.enums.*;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumBoardMapper;
import com.alj.mapper.UserMessageMapper;
import com.alj.pojo.ForumArticle;
import com.alj.mapper.ForumArticleMapper;
import com.alj.pojo.ForumBoard;
import com.alj.pojo.SysSetting;
import com.alj.pojo.UserMessage;
import com.alj.service.ForumArticleService;
import com.alj.service.ForumBoardService;
import com.alj.service.UserInfoService;
import com.alj.service.UserMessageService;
import com.alj.util.FileUtil;
import com.alj.util.StringTools;
import com.alj.util.SysCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * <p>
 * 文章信息 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class ForumArticleServiceImpl extends ServiceImpl<ForumArticleMapper, ForumArticle> implements ForumArticleService {
    @Resource
    private ForumArticleMapper forumArticleMapper;



    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private ForumBoardMapper forumBoardMapper;

    @Resource
    private FileUtil fileUtil;

    @Lazy
    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserMessageMapper userMessageMapper;
    /**
     * @description: 查看某个文章的详情
     * @author alj
     * @date 2024/4/5 21:00
     * @version 1.0
     */
    @Override
    public ForumArticle readArticle(String articleId) {
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId);
        ForumArticle forumArticle=this.forumArticleMapper.selectOne(queryWrapper);
        int Count=forumArticle.getReadCount();
        if (forumArticle==null){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        if (forumArticle.getStatus().equals(1)){
            UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("article_id",articleId);
            ForumArticle update=new ForumArticle();
            //增加阅读数量
            update.setReadCount(Count+1);
            forumArticleMapper.update(update,updateWrapper);
        }
        return forumArticle;
    }
    /**
     * @description: 分页查询
     * @author alj
     * @date 2024/4/5 15:55
     * @version 1.0
     */
    @Override
    public IPage<ForumArticle> selectpage(HttpSession session,Integer boardId, Integer orderType, Integer pBoardId, Integer pageNo) {
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();

        if (boardId != null && boardId != 0) {
            queryWrapper.eq("board_id", boardId);
        }
        // 同上，对pBoardId进行检查
        if (pBoardId != null && pBoardId != 0) {
            queryWrapper.eq("p_board_id", pBoardId);
        }
        //状态1表示已审核
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        queryWrapper.eq("status",1);
        ArticleOrderEnums articleOrderEnums=ArticleOrderEnums.getByType(orderType);
        if (articleOrderEnums != null) {
            String[] orders = articleOrderEnums.getOrderSql().split(",");
            for (String order : orders) {
                String[] parts = order.trim().split(" ");
                if (parts.length == 2) {
                    String field = parts[0];
                    String direction = parts[1];
                    if ("desc".equalsIgnoreCase(direction)) {
                        queryWrapper.orderByDesc(field);
                    } else if ("asc".equalsIgnoreCase(direction)) {
                        queryWrapper.orderByAsc(field);
                    }
                }
            }
        }
        Page<ForumArticle> page=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<ForumArticle> ip=forumArticleMapper.selectPage(page,queryWrapper);
        return ip;
    }

    /**
     * @description: 发布文章
     * @author alj
     * @date 2024/4/12 10:58
     * @version 1.0
     */
    @Override
    public void postArticle(ForumArticle forumArticle, MultipartFile cover, Boolean isAdmin) {
        resetBoardInfo(isAdmin,forumArticle);
        String articleId= StringTools.getRandomString(15);
        Date date=new Date();
        forumArticle.setArticleId(articleId);
        forumArticle.setPostTime(date);
        forumArticle.setLastUpdateTime(date);
        if (cover!=null){
            FileUploadDto dto=fileUtil.uploadFile2local(cover,"/images/", FileUpLoadTypeEnum.ARTICLE_COVER);
            forumArticle.setCover(dto.getLocalPath());
        }
        //文章审核
        if (isAdmin){
            forumArticle.setStatus(ArticleStatusEnums.AUDIT.getStatus());
        }else{
            SysSettingAudit audit= SysCacheUtil.getSysSetting().getSysSettingAudit();
            forumArticle.setStatus(audit.getPostAudit()? ArticleStatusEnums.NO_AUDIT.getStatus() :ArticleStatusEnums.AUDIT.getStatus());
        }
        this.forumArticleMapper.insert(forumArticle);
        //增加发帖积分
        Integer post=SysCacheUtil.getSysSetting().getSysSettingPost().getPostIntegral();
        if (post>0&&ArticleStatusEnums.AUDIT.getStatus().equals(forumArticle.getStatus())){
            this.userInfoService.updateUserIntegral(forumArticle.getUserId(), UserIntegralOpertype.POST_ARTICLE,1,post);
        }
    }

    /**
     * @description: 重置板块信息
     * @author alj
     * @date 2024/4/12 11:36
     * @version 1.0
     */
     private void resetBoardInfo(Boolean isAdmin,ForumArticle forumArticle){
         ForumBoard pBoard = this.forumBoardMapper.selectOne(new QueryWrapper<ForumBoard>().eq("board_id",forumArticle.getPBoardId()));
         if (pBoard==null||pBoard.getPostType()==0&&!isAdmin){
             throw new BusinessException("一级板块不存在");
         }
         forumArticle.setPBoardName(pBoard.getBoardName());
         if (forumArticle.getBoardId()!=null&&forumArticle.getBoardId()!=0){
             ForumBoard board=this.forumBoardMapper.selectOne(new QueryWrapper<ForumBoard>().eq("board_id",forumArticle.getBoardId()));
             if (board==null||board.getPostType()==0&&!isAdmin){
                 throw new BusinessException("二级板块不存在");
             }
             forumArticle.setBoardName(board.getBoardName());
         }else {
             forumArticle.setBoardId(0);
             forumArticle.setBoardName("");
         }
     }

     /**
      * @description: 更新文章
      * @author alj
      * @date 2024/4/12 15:22
      * @version 1.0
      */
    @Override
    public void updateArticle(Boolean isAdmin, ForumArticle article, MultipartFile cover) {
        ForumArticle dbinfo=forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",article.getArticleId()));
        if (!isAdmin&&!dbinfo.getUserId().equals(article.getUserId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        article.setLastUpdateTime(new Date());
        resetBoardInfo(isAdmin,article);
        if (cover!=null) {
            FileUploadDto dto = fileUtil.uploadFile2local(cover, "/images/", FileUpLoadTypeEnum.ARTICLE_COVER);
            article.setCover(dto.getLocalPath());
        }
        //文章是否需要审核
        if (isAdmin){
            article.setStatus(ArticleStatusEnums.AUDIT.getStatus());
        }else{
            SysSettingAudit audit= SysCacheUtil.getSysSetting().getSysSettingAudit();
            article.setStatus(audit.getPostAudit()? ArticleStatusEnums.NO_AUDIT.getStatus() :ArticleStatusEnums.AUDIT.getStatus());
        }
        UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("article_id",dbinfo.getArticleId());
        this.forumArticleMapper.update(article,updateWrapper);
    }

    public IPage<ForumArticle> getArticlesByUserComments(String userId, int currentPage, int pageSize) {
        Page<ForumArticle> page = new Page<>(currentPage, pageSize);
        return forumArticleMapper.selectArticlesByUserComments(page, userId);
    }

    @Override
    public IPage<ForumArticle> getUserlike(String userId, int currentPage, int pageSize) {
        Page<ForumArticle> page = new Page<>(currentPage, pageSize);
        return forumArticleMapper.selectByLike(page, userId);
    }

    /**
     * @description: 删除全部的数据
     * @author alj
     * @date 2024/4/15 9:37
     * @version 1.0
     */
    @Override
    public void delArticle(String articleIds) {
        String[] articleIdArray=articleIds.split(",");
        for (String articleId:articleIdArray){
            forumArticleService.delArticleSingle(articleId);
        }
    }


    /**
     * @description: 删除单独的文章
     * @author alj
     * @date 2024/4/15 9:37
     * @version 1.0
     */
    @Transactional(rollbackFor = Exception.class)
    public void delArticleSingle(String articleId){
        ForumArticle forumArticle=this.forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",articleId));
        if (forumArticle==null||ArticleStatusEnums.DEL.getStatus().equals(forumArticle.getStatus())){
            return;
        }
        ForumArticle update=new ForumArticle();
        update.setStatus(ArticleStatusEnums.DEL.getStatus());
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId);
        forumArticleMapper.update(update,queryWrapper);
        Integer integral=SysCacheUtil.getSysSetting().getSysSettingPost().getPostIntegral();
        //减少积分
        if (integral>0&&ArticleStatusEnums.AUDIT.getStatus().equals(forumArticle.getStatus())){
            userInfoService.updateUserIntegral(forumArticle.getUserId(),UserIntegralOpertype.DEL_ARTICLE,-1,integral);
        }
        //发送消息
        UserMessage userMessage = new UserMessage();
        userMessage.setReceivedUserId(forumArticle.getUserId());
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageStatus.NO_READ.getStatus());
        userMessage.setMessageContent("文章【"+forumArticle.getTitle()+"】被管理员删除！");
        this.userMessageMapper.insert(userMessage);
    }


    /**
     * @description: 更新文章所在的板块
     * @author alj
     * @date 2024/4/15 10:32
     * @version 1.0
     */
    @Override
    public void updateBoard(String articleId, Integer PBoardId, Integer boardId) {
        ForumArticle forumArticle=new ForumArticle();
        forumArticle.setPBoardId(PBoardId);
        forumArticle.setBoardId(boardId);
        resetBoardInfo(true,forumArticle);
        UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("article_id",articleId);
        forumArticleMapper.update(forumArticle,updateWrapper);
    }


    /**
     * @description: 审核文章
     * @author alj
     * @date 2024/4/15 14:20
     * @version 1.0
     */
    @Override
    public void auditArticle(String articleIds) {
        String[] articleIdArray=articleIds.split(",");
        for (String articleId:articleIdArray){
            forumArticleService.auditArticleSingle(articleId);
        }
    }


    /**
     * @description: 审核单个文章
     * @author alj
     * @date 2024/4/15 14:24
     * @version 1.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditArticleSingle(String articleId) {
        ForumArticle forumArticle=forumArticleMapper.selectOne(new QueryWrapper<ForumArticle>().eq("article_id",articleId));
        if (forumArticle==null||!forumArticle.getStatus().equals(ArticleStatusEnums.NO_AUDIT.getStatus())){
            return;
        }
        ForumArticle update = new ForumArticle();
        forumArticle.setStatus(ArticleStatusEnums.AUDIT.getStatus());
        UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("article_id",articleId);
        forumArticleMapper.update(update,updateWrapper);

        Integer integral= SysCacheUtil.getSysSetting().getSysSettingPost().getPostIntegral();
        if (integral>0&&ArticleStatusEnums.AUDIT.getStatus().equals(forumArticle.getStatus())){
            userInfoService.updateUserIntegral(forumArticle.getUserId(),UserIntegralOpertype.POST_ARTICLE,1,integral);
        }
    }


}
