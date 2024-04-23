package com.alj.service;

import com.alj.pojo.LikeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 点赞记录 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface LikeRecordService extends IService<LikeRecord> {
    LikeRecord getLikeRecordByOUT(String articleId,String userId,Integer Type);

    void dolike(String ObjectId,String userId,String nickName,Integer opType);

}
