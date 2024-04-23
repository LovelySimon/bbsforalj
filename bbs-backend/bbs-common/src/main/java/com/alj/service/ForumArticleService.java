package com.alj.service;

import com.alj.pojo.ForumArticle;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 文章信息 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface ForumArticleService extends IService<ForumArticle> {
    void updateArticle(Boolean isAdmin,ForumArticle article,MultipartFile cover);

    void postArticle(ForumArticle forumArticle, MultipartFile cover,Boolean isAdmin);

    ForumArticle readArticle(String articleId);

    IPage<ForumArticle> getArticlesByUserComments(String userId, int currentPage, int pageSize);


    IPage<ForumArticle> getUserlike(String userId, int currentPage, int pageSize);

    void updateBoard(String articleId,Integer PBoardId,Integer boardId);

    void delArticle(String articleIds);
    void delArticleSingle(String articleId);

    void auditArticle(String articleIds);
    void auditArticleSingle(String articleId);

    IPage<ForumArticle> selectpage(HttpSession session,Integer boardId, Integer orderType, Integer pBoardId, Integer pageNo);
}
