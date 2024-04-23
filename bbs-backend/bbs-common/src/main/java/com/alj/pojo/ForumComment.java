package com.alj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 评论
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ForumComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 父级评论ID
     */
    private Integer pCommentId;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 图片
     */
    private String imgPath;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 用户ip地址
     */
    private String userIpAddress;

    /**
     * 回复人ID
     */
    private String replyUserId;

    /**
     * 回复人昵称
     */
    private String replyNickName;

    /**
     * 0:未置顶  1:置顶
     */
    private Integer topType;

    /**
     * 发布时间
     */
    private Date postTime;

    /**
     * good数量
     */
    private Integer goodCount;

    /**
     * 0:待审核  1:已审核
     */
    private Integer status;

    @TableField(exist = false)
    private List<ForumComment> secondaryComments;

    @TableField(exist = false)
    private Boolean LikeType;

    public List<ForumComment> getSecondaryComments() {
        return secondaryComments;
    }

    public void setSecondaryComments(List<ForumComment> secondaryComments) {
        this.secondaryComments = secondaryComments;
    }

    public Boolean getLikeType() {
        return LikeType;
    }

    public void setLikeType(Boolean likeType) {
        LikeType = likeType;
    }
}
