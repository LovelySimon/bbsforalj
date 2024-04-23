package com.alj.dto;

/**
 * @description: 审核设置
 * @author alj
 * @date 2024/4/2 14:22
 * @version 1.0
 */
public class SysSettingAudit {
    //帖子是否需要审核
    private Boolean commentAudit;

    //评论是否需要审核
    private Boolean postAudit;

    public Boolean getCommentAudit() {
        return commentAudit;
    }

    public void setCommentAudit(Boolean commentAudit) {
        this.commentAudit = commentAudit;
    }

    public Boolean getPostAudit() {
        return postAudit;
    }

    public void setPostAudit(Boolean postAudit) {
        this.postAudit = postAudit;
    }
}
