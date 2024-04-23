package com.alj.service.impl;

import com.alj.dto.UserMessageCountDto;
import com.alj.enums.MessageStatus;
import com.alj.enums.MessageTypeEnum;
import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import com.alj.pojo.UserMessage;
import com.alj.mapper.UserMessageMapper;
import com.alj.service.UserMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户消息 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {
    @Resource
    private UserMessageMapper userMessageMapper;

    @Override
    public UserMessageCountDto getUserMessageCount(String userId) {
        List<Map> mapList=userMessageMapper.getuserMessageCount(userId);
        UserMessageCountDto userMessageCountDto=new UserMessageCountDto();
        Long totalCount=0L;
        for (Map item:mapList){
            Integer type =  (Integer) item.get("messageType");
            Long count = (Long) item.get("count");
            totalCount=totalCount+count;
            MessageTypeEnum messageTypeEnum=MessageTypeEnum.getByType(type);
            switch (messageTypeEnum){
                case SYS:
                    userMessageCountDto.setSys(count);
                    break;
                case COMMENT:
                    userMessageCountDto.setReply(count);
                    break;
                case ARTICLE_LIKE:
                    userMessageCountDto.setLikePost(count);
                    break;
                case COMMENT_LIKE:
                    userMessageCountDto.setLikeComment(count);
                    break;
            }

        }
        userMessageCountDto.setTotal(totalCount);
        return userMessageCountDto;
    }

    @Override
    public void readMessageByType(String userId, Integer type) {
        this.userMessageMapper.updateMessageStatus(userId,type, MessageStatus.READ.getStatus());
    }
}
