package com.alj.vo;

import com.alj.pojo.ForumArticle;

public class ForumArticleUpdateDetailVO {
    private ForumArticle forumArticle;
    private ForumArticleAttachmentVo attachment;

    public ForumArticle getForumArticle() {
        return forumArticle;
    }

    public void setForumArticle(ForumArticle forumArticle) {
        this.forumArticle = forumArticle;
    }

    public ForumArticleAttachmentVo getAttachment() {
        return attachment;
    }

    public void setAttachment(ForumArticleAttachmentVo attachment) {
        this.attachment = attachment;
    }
}
