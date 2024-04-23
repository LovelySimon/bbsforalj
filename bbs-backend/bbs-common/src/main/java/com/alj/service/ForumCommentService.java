package com.alj.service;

import com.alj.pojo.ForumComment;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * <p>
 * 评论 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface ForumCommentService extends IService<ForumComment> {
    void auditComment(String commentIds);

    void auditCommentSingle(Integer commentId);


    void delComment(String commentIds);

    void delCommentSingle(Integer commentId);

    void selectChildren(List<ForumComment> primaryComments);
    void changeTopType(String userId,String commentId,Integer topType);
    void postComment(ForumComment comment, MultipartFile file);
    IPage<ForumComment> selectpage(HttpSession session,String articleId,Integer pageNo,Integer orderType);
}
