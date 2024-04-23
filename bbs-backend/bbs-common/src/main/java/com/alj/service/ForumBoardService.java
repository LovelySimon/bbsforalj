package com.alj.service;

import com.alj.pojo.ForumBoard;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 文章板块信息 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface ForumBoardService extends IService<ForumBoard> {
    List<ForumBoard> getBoardTree(Integer postType);

    void saveForumBoard(ForumBoard forumBoard);

    void changeSort(String boardIds);
}
