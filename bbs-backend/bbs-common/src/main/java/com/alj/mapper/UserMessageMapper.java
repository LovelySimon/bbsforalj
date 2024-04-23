package com.alj.mapper;

import com.alj.pojo.UserMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户消息 Mapper 接口
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface UserMessageMapper extends BaseMapper<UserMessage> {
    List<Map> getuserMessageCount(String userId);

    void updateMessageStatus(@Param("receivedUserId")String receivedUserId,@Param("messageType")Integer messageType,@Param("status")Integer status);
}
