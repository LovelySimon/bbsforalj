package com.alj.service.impl;

import com.alj.mapper.ForumArticleMapper;
import com.alj.mapper.LikeRecordMapper;
import com.alj.mapper.UserArticleRecommendMapper;
import com.alj.mapper.UserInfoMapper;
import com.alj.pojo.ForumArticle;
import com.alj.pojo.LikeRecord;
import com.alj.pojo.UserArticleRecommend;
import com.alj.pojo.UserInfo;
import com.alj.service.UserArticleRecommendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserArticleRecommendServiceImpl extends ServiceImpl<UserArticleRecommendMapper, UserArticleRecommend> implements UserArticleRecommendService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private LikeRecordMapper likeRecordMapper;

    @Resource
    private UserArticleRecommendMapper userArticleRecommendMapper;

    @Lazy
    @Resource
    private UserArticleRecommendService userArticleRecommendService;

    @Resource
    private ForumArticleMapper forumArticleMapper;
    /**
     * @description: 计算每个用户对应文章的value并添加到数据库用于推荐算法
     * @author alj
     * @date 2024/4/19 18:45
     * @version 1.0
     */
    @Override
    public void calAllUserPreference() {
        List<UserInfo> userList=userInfoMapper.selectList(new QueryWrapper<>());
        for (UserInfo user:userList){
            System.out.println("处理用户ID: " + user.getUserId());
            List<LikeRecord> likelist = this.likeRecordMapper.selectList(new QueryWrapper<LikeRecord>().eq("user_id",user.getUserId()));
            for (LikeRecord likeRecord:likelist){
                if (likeRecord.getObjectId().length()<6){
                    continue;
                }
                UserArticleRecommend userArticleRecommend=userArticleRecommendMapper.selectOne(new QueryWrapper<UserArticleRecommend>().eq("user_id",likeRecord.getUserId()).eq("article_id",likeRecord.getObjectId()));
                if (userArticleRecommend==null){
                        UserArticleRecommend userArticleRecommend1=new UserArticleRecommend();
                        userArticleRecommend1.setUserId(user.getUserId());
                        userArticleRecommend1.setArticleId(likeRecord.getObjectId());
                        //点赞加2系数
                        userArticleRecommend1.setValue(2);
                        userArticleRecommendMapper.insert(userArticleRecommend1);
                }
                if (userArticleRecommend!=null){
                    Integer value=userArticleRecommend.getValue();
                    UserArticleRecommend update=new UserArticleRecommend();
                    update.setValue(value+2);
                    UpdateWrapper<UserArticleRecommend> updateWrapper=new UpdateWrapper<>();
                    updateWrapper.eq("id",userArticleRecommend.getId());
                    this.userArticleRecommendMapper.update(update,updateWrapper);
                }
            }
        }
        List<UserArticleRecommend> allUserRecommendations = userArticleRecommendMapper.selectList(new QueryWrapper<>());
        for (UserArticleRecommend recommendation : allUserRecommendations) {
            System.out.println("UserID: " + recommendation.getUserId() + ", ArticleID: " + recommendation.getArticleId() + ", Value: " + recommendation.getValue());
        }
    }


    /**
     * @description: 手动创建一个处理ID映射的版本的DataModel构建方法
     * @author alj
     * @date 2024/4/21 16:06
     * @version 1.0
     */

    public DataModel createDataModelWithMapping(List<UserArticleRecommend> userArticleRecommends, Map<String, Long> articleIdToLong) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<Preference>> userPreferencesMap = new HashMap<>();

        // 遍历所有推荐，使用映射的文章ID构建偏好数据
        for (UserArticleRecommend userPreference : userArticleRecommends) {
            long userId = Long.parseLong(userPreference.getUserId());  // 假设用户ID已经是长整型或可直接转换
            long articleId = articleIdToLong.getOrDefault(userPreference.getArticleId(), -1L);
            if (articleId == -1L) continue;
            float value = userPreference.getValue();

            // 按用户ID将偏好添加到对应的列表中
            userPreferencesMap.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new GenericPreference(userId, articleId, value));
        }

        // 将每个用户的偏好列表转换成PreferenceArray，然后放入FastByIDMap中
        for (Map.Entry<Long, List<Preference>> entry : userPreferencesMap.entrySet()) {
            List<Preference> prefs = entry.getValue();
            if (!prefs.isEmpty()) {
                PreferenceArray prefArray = new GenericUserPreferenceArray(prefs);
                fastByIdMap.put(entry.getKey(), prefArray);
            }
        }
        System.out.println("创建数据模型，用户数量: " + userPreferencesMap.size());
        return new GenericDataModel(fastByIdMap);
    }

    /**
     * @description: 基于用户的协同过滤算法
     * @author alj
     * @date 2024/4/19 19:51
     * @version 1.0
     */
    @Override
    public List<String> recommend(String userId) throws TasteException {
        calAllUserPreference();
        System.out.println("推荐处理的用户ID: " + userId);
        List<UserArticleRecommend> userList = userArticleRecommendMapper.selectList(new QueryWrapper<UserArticleRecommend>());
        Map<String, Long> articleIdToLong = new HashMap<>();
        Map<Long, String> longToArticleId = new HashMap<>();
        long index = 1L;
        // 创建文章ID映射
        for (UserArticleRecommend article : userList) {
            if (!articleIdToLong.containsKey(article.getArticleId())) {
                articleIdToLong.put(article.getArticleId(), index);
                longToArticleId.put(index, article.getArticleId());
                index++;
            }
        }
        // 创建数据模型
        DataModel dataModel = this.createDataModelWithMapping(userList, articleIdToLong);

        // 其他推荐系统设置
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(1, similarity, dataModel);
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);

        // 推荐文章
        List<RecommendedItem> recommendedItems = recommender.recommend(Long.parseLong(userId), 1  );
        List<String> itemIds = recommendedItems.stream()
                .map(item -> longToArticleId.get(item.getItemID()))
                .collect(Collectors.toList());
        System.out.println(itemIds);
        return itemIds;
    }



    /**
     * @description: 创建数据模型
     * @author alj
     * @date 2024/4/19 19:51
     * @version 1.0
     */
    @Override
    public DataModel createDataModel(List<UserArticleRecommend> userArticleRecommends) {
        Map<String, Long> userIdToLong = new HashMap<>();
        Map<String, Long> articleIdToLong = new HashMap<>();
        // 假设这两个映射表已经被初始化
        initializeIdMaps(userArticleRecommends,userIdToLong,articleIdToLong); // 确保映射是最新的

        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        for (UserArticleRecommend userPreference : userArticleRecommends) {
            long userId = userIdToLong.get(userPreference.getUserId());
            long articleId = articleIdToLong.get(userPreference.getArticleId());
            float value = userPreference.getValue();

            // 创建新的偏好列表，如果已存在偏好则复制它们
            List<Preference> prefsList = new ArrayList<>();
            if (fastByIdMap.containsKey(userId)) {
                // 复制现有偏好到新列表
                PreferenceArray existingPrefs = fastByIdMap.get(userId);
                for (int i = 0; i < existingPrefs.length(); i++) {
                    prefsList.add(existingPrefs.get(i));
                }
            }

            // 添加新偏好
            prefsList.add(new GenericPreference(userId, articleId, value));
            // 创建新的PreferenceArray并更新映射
            PreferenceArray prefsArray = new GenericUserPreferenceArray(prefsList);
            fastByIdMap.put(userId, prefsArray);
        }

        return new GenericDataModel(fastByIdMap);
    }

    private void initializeIdMaps(List<UserArticleRecommend> userArticleRecommends, Map<String, Long> userIdToLong, Map<String, Long> articleIdToLong) {
        long userIndex = 1L, articleIndex = 1L;
        for (UserArticleRecommend rec : userArticleRecommends) {
            userIdToLong.putIfAbsent(rec.getUserId(), userIndex++);
            articleIdToLong.putIfAbsent(rec.getArticleId(), articleIndex++);
        }
    }

    public IPage<ForumArticle> getRecommend(String userId, int currentPage, int pageSize) throws TasteException {
        List<String> articleIds = userArticleRecommendService.recommend(userId);
        // 利用MyBatis-Plus的Page类来处理分页
        Page<ForumArticle> page = new Page<>(currentPage, pageSize);
        // 查询条件
        QueryWrapper<ForumArticle> queryWrapper = new QueryWrapper<>();
        if (articleIds.isEmpty()) {
            return forumArticleMapper.selectPage(page,queryWrapper);
        }
        queryWrapper.in("article_id", articleIds);
        // 确保按照推荐列表的顺序返回
        queryWrapper.orderBy(true, true, "field(article_id," + String.join(",", articleIds) + ")");

        // 执行分页查询
        IPage<ForumArticle> result = forumArticleMapper.selectPage(page, queryWrapper);
        return result;
    }
}
