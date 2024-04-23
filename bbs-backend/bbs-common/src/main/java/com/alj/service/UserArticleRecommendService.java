package com.alj.service;

import com.alj.pojo.ForumArticle;
import com.alj.pojo.UserArticleRecommend;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;

import java.util.List;

public interface UserArticleRecommendService extends IService<UserArticleRecommend> {
    void  calAllUserPreference();
    List<String> recommend(String userId) throws TasteException;

    DataModel createDataModel(List<UserArticleRecommend> userArticleRecommends);

    IPage<ForumArticle> getRecommend(String userId, int currentPage, int pageSize) throws TasteException;
}
