package com.alj.mapper;

import com.alj.pojo.ForumArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 文章信息 Mapper 接口
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface ForumArticleMapper extends BaseMapper<ForumArticle> {
    @Select("SELECT * FROM forum_article WHERE article_id IN (SELECT article_id FROM forum_comment WHERE user_id = #{userId})")
    IPage<ForumArticle> selectArticlesByUserComments(Page<ForumArticle> page, @Param("userId") String userId);
    @Select("SELECT * FROM forum_article WHERE article_id IN (SELECT object_id FROM like_record WHERE user_id = #{userId})")
    IPage<ForumArticle> selectByLike(Page<ForumArticle> page, @Param("userId") String userId);

    void updateArticleByUserId(@Param("status")Integer status,@Param("userId")String userId);

    void updateBoardNameBatch(@Param("boardType")Integer boardType,@Param("boardName")String boardName,@Param("boardId")Integer boardId);
}
