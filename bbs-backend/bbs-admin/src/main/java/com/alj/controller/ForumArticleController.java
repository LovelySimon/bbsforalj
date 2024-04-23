package com.alj.controller;

import com.alj.mapper.ForumArticleMapper;
import com.alj.mapper.ForumCommentMapper;
import com.alj.pojo.ForumArticle;
import com.alj.pojo.ForumComment;
import com.alj.service.ForumArticleService;
import com.alj.service.ForumCommentService;
import com.alj.util.StringTools;
import com.alj.vo.PaginationResultVO;
import com.alj.vo.ResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController()
@RequestMapping("/forum")
public class ForumArticleController extends ABaseController{
    private static final Logger logger = LoggerFactory.getLogger(ForumArticleController.class);

    @Resource
    private ForumArticleMapper forumArticleMapper;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumCommentMapper forumCommentMapper;

    @Resource
    private ForumCommentService forumCommentService;


    /**
     * @description:  获取文章管理
     * @author alj
     * @date 2024/4/14 20:04
     * @version 1.0
     */
    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(Integer pageNo,String titleFuzzy,String nickNameFuzzy,Integer status,Integer topType,Integer pBoardId,Integer boardId){
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        if (!StringTools.isEmpty(titleFuzzy)){
            queryWrapper.like("title",titleFuzzy);
        }
        if (!StringTools.isEmpty(nickNameFuzzy)){
            queryWrapper.like("nick_name",nickNameFuzzy);
        }
        if (status!=null){
            queryWrapper.eq("status",status);
        }
        if (pBoardId!=null){
            queryWrapper.eq("p_board_id",pBoardId);
        }
        if (boardId!=null){
            queryWrapper.eq("board_id",boardId);
        }
        if (topType!=null){
            queryWrapper.eq("top_type",topType);
        }
        Page<ForumArticle> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<ForumArticle> pageResult=forumArticleMapper.selectPage(pageinfo,queryWrapper);
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
     * @description: 删除文章
     * @author alj
     * @date 2024/4/15 10:18
     * @version 1.0
     */
    @RequestMapping("/delArticle")
    public ResponseVO delArticle(String articleIds){
        forumArticleService.delArticle(articleIds);
        return getSuccessResponseVO(null);
    }


    /**
     * @description: 更新文章的板块信息
     * @author alj
     * @date 2024/4/15 10:20
     * @version 1.0
     */
    @RequestMapping("/updateBoard")
    public ResponseVO updateBoard(String articleId,Integer pBoardId,Integer boardId){
        boardId = boardId==null?0:boardId;
        forumArticleService.updateBoard(articleId,pBoardId,boardId);
        return getSuccessResponseVO(null);
    }


    /**
     * @description:置顶文章
     * @author alj
     * @date 2024/4/15 11:16
     * @version 1.0
     */
    @RequestMapping("/topArticle")
    public ResponseVO topArticle(String articleId,Integer topType){
        ForumArticle forumArticle=new ForumArticle();
        forumArticle.setTopType(topType);
        UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("article_id",articleId);
        forumArticleMapper.update(forumArticle,updateWrapper);
        return getSuccessResponseVO(null);
    }


    /**
     * @description: 文章审核
     * @author alj
     * @date 2024/4/15 14:28
     * @version 1.0
     */
    @RequestMapping("/auditArticle")
    public ResponseVO auditArticle(String articleIds){
        forumArticleService.auditArticle(articleIds);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 加载所有评论以及模糊查询
     * @author alj
     * @date 2024/4/15 15:23
     * @version 1.0
     */
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo,String contentFuzzy,String nickNameFuzzy,Integer status){
        QueryWrapper<ForumComment> queryWrapper=new QueryWrapper<>();
        if (contentFuzzy!=null){
            queryWrapper.like("content",contentFuzzy);
        }
        if (nickNameFuzzy!=null){
            queryWrapper.like("nick_name",nickNameFuzzy);
        }
        Page<ForumComment> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<ForumComment> pageResult=forumCommentMapper.selectPage(pageinfo,queryWrapper);
        if (pageResult.getRecords() != null && !pageResult.getRecords().isEmpty()) {
            forumCommentService.selectChildren(pageResult.getRecords());
        }
        PaginationResultVO<ForumComment> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 加载文章评论
     * @author alj
     * @date 2024/4/15 15:23
     * @version 1.0
     */
    @RequestMapping("/loadComment4Article")
    public ResponseVO loadComment4Article(Integer pageNo,String articleId){
        QueryWrapper<ForumComment> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId);
        queryWrapper.eq("p_comment_id",0);
        List<ForumComment> forumComments=this.forumCommentMapper.selectList(queryWrapper);
        return getSuccessResponseVO(forumComments);
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(String commentIds){
        forumCommentService.delComment(commentIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/auditComment")
    public ResponseVO auditComment(String commentIds){
        forumCommentService.auditComment(commentIds);
        return getSuccessResponseVO(null);
    }
}
