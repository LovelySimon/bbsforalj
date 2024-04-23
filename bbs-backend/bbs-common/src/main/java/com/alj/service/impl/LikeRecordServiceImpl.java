package com.alj.service.impl;

import com.alj.enums.MessageStatus;
import com.alj.enums.MessageTypeEnum;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumArticleMapper;
import com.alj.mapper.ForumCommentMapper;
import com.alj.mapper.UserMessageMapper;
import com.alj.pojo.ForumArticle;
import com.alj.pojo.ForumComment;
import com.alj.pojo.LikeRecord;
import com.alj.mapper.LikeRecordMapper;
import com.alj.pojo.UserMessage;
import com.alj.service.LikeRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 点赞记录 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {
    @Resource
    private LikeRecordMapper likeRecordMapper;

    @Resource
    private UserMessageMapper userMessageMapper;

    @Resource
    private ForumArticleMapper forumArticleMapper;
    @Resource
    private ForumCommentMapper forumCommentMapper;
    @Override
    public LikeRecord getLikeRecordByOUT(String articleId, String userId, Integer Type) {
        QueryWrapper<LikeRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("object_id",articleId).eq("user_id",userId).eq("op_type",Type);
        LikeRecord likeRecord=likeRecordMapper.selectOne(queryWrapper);
        return likeRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dolike(String ObjectId, String userId, String nickName, Integer opType) {
        UserMessage userMessage=new UserMessage();
        userMessage.setCreateTime(new Date());
        LikeRecord likeRecord=null;
        //文章点赞
        if (opType==0){
            ForumArticle forumArticle=forumArticleMapper.selectById(ObjectId);
            if (forumArticle==null){
                throw  new BusinessException("文章不存在");
            }
            likeRecord=articleLike(ObjectId,userId,0);
            userMessage.setArticleId(ObjectId);
            userMessage.setArticleTitle(forumArticle.getTitle());
            userMessage.setMessageType(MessageTypeEnum.ARTICLE_LIKE.getType());
            userMessage.setCommentId(0);
            userMessage.setReceivedUserId(forumArticle.getUserId());
        }
        //评论点赞
        if (opType==1){
            ForumComment forumComment= forumCommentMapper.selectById(Integer.parseInt(ObjectId));
            if (forumComment==null){
                throw new BusinessException("评论不存在");
            }
            likeRecord=CommentLike(ObjectId,userId,1);
            ForumArticle forumArticle=forumArticleMapper.selectById(String.valueOf(forumComment.getArticleId()));
            userMessage.setArticleId(ObjectId);
            userMessage.setArticleTitle(forumArticle.getTitle());
            userMessage.setMessageType(MessageTypeEnum.COMMENT_LIKE.getType());
            userMessage.setCommentId(forumComment.getCommentId());
            userMessage.setReceivedUserId(forumComment.getUserId());
            userMessage.setMessageContent(forumComment.getContent());
        }
        userMessage.setSendUserId(userId);
        userMessage.setSendNickName(nickName);
        userMessage.setStatus(MessageStatus.NO_READ.getStatus());
        if (!userId.equals(userMessage.getReceivedUserId())) {
            UserMessage last = new UserMessage();
            last=null;
            if (opType==0){
                QueryWrapper<UserMessage>  userMessageQueryWrapper=new QueryWrapper<>();
                userMessageQueryWrapper.eq("article_id",ObjectId).eq("message_type",MessageTypeEnum.ARTICLE_LIKE.getType()).eq("send_user_id",userId).eq("comment_id",userMessage.getCommentId());
                last=userMessageMapper.selectOne(userMessageQueryWrapper);
            }
            if(opType==1){
                QueryWrapper<UserMessage>  userMessageQueryWrapper=new QueryWrapper<>();
                userMessageQueryWrapper.eq("article_id",ObjectId).eq("message_type",MessageTypeEnum.COMMENT_LIKE.getType()).eq("send_user_id",userId).eq("comment_id",userMessage.getCommentId());
                 last=userMessageMapper.selectOne(userMessageQueryWrapper);
            }
            if (last==null){
                userMessageMapper.insert(userMessage);
            }
        }
    }

    public LikeRecord articleLike(String objectId,String userId,Integer type){
        QueryWrapper<LikeRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("object_id",objectId).eq("user_id",userId).eq("op_type",type);
        LikeRecord record=likeRecordMapper.selectOne(queryWrapper);
        if (record!=null){
            this.likeRecordMapper.delete(queryWrapper);
            QueryWrapper<ForumArticle> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.eq("article_id",objectId);
            ForumArticle forumArticle=this.forumArticleMapper.selectOne(queryWrapper1);
            UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("article_id",objectId);
            ForumArticle update=new ForumArticle();
            update.setGoodCount(forumArticle.getGoodCount()-1);
            this.forumArticleMapper.update(update,updateWrapper);
        }else{
            QueryWrapper<ForumArticle> last=new QueryWrapper<>();
            last.eq("article_id",objectId);
            ForumArticle forumArticle=this.forumArticleMapper.selectOne(last);
            if (null==forumArticle){
                throw new BusinessException("文章不存在");
            }
            LikeRecord likeRecord=new LikeRecord();
            likeRecord.setObjectId(objectId);
            likeRecord.setUserId(userId);
            likeRecord.setOpType(type);
            likeRecord.setCreateTime(new Date());
            likeRecord.setAuthorUserId(forumArticle.getUserId());
            this.likeRecordMapper.insert(likeRecord);
            QueryWrapper<ForumArticle> wrapper=new QueryWrapper<>();
            wrapper.eq("article_id",objectId);
            ForumArticle article=this.forumArticleMapper.selectOne(wrapper);
            UpdateWrapper<ForumArticle> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("article_id",objectId);
            ForumArticle article1=new ForumArticle();
            article1.setGoodCount(article.getGoodCount()+1);
            this.forumArticleMapper.update(article1,updateWrapper);
        }
        return record;
    }

    /**
     * @description: 更新评论点赞数
     * @author alj
     * @date 2024/4/9 13:05
     * @version 1.0
     */
    public LikeRecord CommentLike(String objectId,String userId,Integer opType){
        QueryWrapper<LikeRecord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("object_id",objectId).eq("user_id",userId).eq("op_type",opType);
        LikeRecord record=likeRecordMapper.selectOne(queryWrapper);
        //取消点赞
        if (record!=null){
            this.likeRecordMapper.delete(queryWrapper);
            QueryWrapper<ForumComment> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("comment_id",Integer.parseInt(objectId));
            UpdateWrapper<ForumComment> updateWrapper=new UpdateWrapper<>();
            ForumComment last=this.forumCommentMapper.selectOne(queryWrapper1);
            updateWrapper.eq("comment_id",Integer.parseInt(objectId));
            ForumComment update=new ForumComment();
            update.setGoodCount(last.getGoodCount()-1);
            this.forumCommentMapper.update(update,updateWrapper);
        }else{
            ForumComment forumComment=this.forumCommentMapper.selectOne(new QueryWrapper<ForumComment>().eq("comment_id",Integer.parseInt(objectId)));
            if (forumComment==null){
                throw new BusinessException("评论不存在");
            }
            LikeRecord likeRecord=new LikeRecord();
            likeRecord.setObjectId(objectId);
            likeRecord.setUserId(userId);
            likeRecord.setOpType(opType);
            likeRecord.setCreateTime(new Date());
            likeRecord.setAuthorUserId(forumComment.getUserId());
            this.likeRecordMapper.insert(likeRecord);
            QueryWrapper<ForumComment> wrapper=new QueryWrapper<>();
            wrapper.eq("comment_id",Integer.parseInt(objectId));
            ForumComment comment=this.forumCommentMapper.selectOne(wrapper);
            UpdateWrapper<ForumComment> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("comment_id",Integer.parseInt(objectId));
            ForumComment update=new ForumComment();
            update.setGoodCount(comment.getGoodCount()+1);
            this.forumCommentMapper.update(update,updateWrapper);
        }
        return record;
    }
}
