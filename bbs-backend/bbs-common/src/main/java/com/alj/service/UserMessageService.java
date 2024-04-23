package com.alj.service;

import com.alj.dto.UserMessageCountDto;
import com.alj.pojo.UserMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户消息 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface UserMessageService extends IService<UserMessage> {
     UserMessageCountDto getUserMessageCount(String userId);
     void readMessageByType(String userId,Integer type);
}
