package com.alj.controller;

import com.alj.dto.SessionWebloginDto;
import com.alj.enums.EditorTypeEnums;
import com.alj.enums.ResponseCodeEnum;
import com.alj.exception.BusinessException;
import com.alj.mapper.ForumArticleMapper;
import com.alj.pojo.ForumArticle;
import com.alj.pojo.ForumBoard;
import com.alj.pojo.LikeRecord;
import com.alj.pojo.UserMessage;
import com.alj.service.ForumArticleService;
import com.alj.service.ForumBoardService;
import com.alj.service.LikeRecordService;
import com.alj.service.UserArticleRecommendService;
import com.alj.util.CopyTools;
import com.alj.util.StringTools;
import com.alj.vo.ForumArticleDetailVO;
import com.alj.vo.ForumArticleVO;
import com.alj.vo.PaginationResultVO;
import com.alj.vo.ResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/forum")
public class ForumArticleController extends ABaseController {
    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private ForumArticleMapper forumArticleMapper;

    @Resource
    private UserArticleRecommendService userArticleRecommendService;
    /**
     * @description: 加载文章列表
     * @author alj
     * @date 2024/4/12 10:15
     * @version 1.0
     */
    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(HttpSession session,Integer boardId,Integer orderType,Integer pBoardId,Integer pageNo) throws TasteException {
        PaginationResultVO<ForumArticle> resultVO=new PaginationResultVO<>();
        if (orderType==3){
            SessionWebloginDto sessionWebloginDto=(SessionWebloginDto)session.getAttribute("session_key");
            IPage<ForumArticle> pageResult = userArticleRecommendService.getRecommend(sessionWebloginDto.getUserId(),pageNo,5);
                    resultVO = new PaginationResultVO<>(
                    (int) pageResult.getTotal(), // 总记录数
                    (int) pageResult.getSize(), // 每页记录数
                    (int) pageResult.getCurrent(), // 当前页号
                    (int) pageResult.getPages(),// 总页数
                    pageResult.getRecords() // 当前页数据
            );
        }else {
            IPage<ForumArticle> pageResult = forumArticleService.selectpage(session,boardId, orderType , pBoardId, pageNo);
                    resultVO = new PaginationResultVO<>(
                    (int) pageResult.getTotal(), // 总记录数
                    (int) pageResult.getSize(), // 每页记录数
                    (int) pageResult.getCurrent(), // 当前页号
                    (int) pageResult.getPages(),// 总页数
                    pageResult.getRecords() // 当前页数据
            );
        }
        // 创建PaginationResultVO实例
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 获取文章详情
     * @author alj
     * @date 2024/4/12 10:14
     * @version 1.0
     */
    @RequestMapping("/getArticleDetail")
    public ResponseVO getArticleDetail(HttpSession session,String articleId){
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        ForumArticle forumArticle=forumArticleService.readArticle(articleId);
       if (null==forumArticle||(forumArticle.getStatus().equals(0))&& (sessionWebloginDto==null||!sessionWebloginDto.getUserId().equals(forumArticle.getUserId())||!sessionWebloginDto.getAdmin())){
           throw new BusinessException(ResponseCodeEnum.CODE_404);
       }

        ForumArticleDetailVO detailVO=new ForumArticleDetailVO();
        detailVO.setForumArticle(CopyTools.copy(forumArticle, ForumArticleVO.class));

        //文章是否被点赞
        if (sessionWebloginDto!=null){
            String userId=sessionWebloginDto.getUserId();
            LikeRecord likeRecord = likeRecordService.getLikeRecordByOUT(articleId,userId,0);
            if(likeRecord!=null){
                detailVO.setHaveLike(true);
            }
        }
        return getSuccessResponseVO(detailVO);
    }
    @RequestMapping("/Like")
    public ResponseVO Like(HttpSession session,String articleId){
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        likeRecordService.dolike(articleId,sessionWebloginDto.getUserId(),sessionWebloginDto.getNickName(),0);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 展示板块信息，非管理员和管理员板块有差异
     * @author alj
     * @date 2024/4/12 10:14
     * @version 1.0
     */
    @RequestMapping("/loadBoard4Post")
    public ResponseVO loadBoard4Post(HttpSession session){

        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        Integer postType=null;
        if (!sessionWebloginDto.getAdmin()){
            postType=1;
        }
        List<ForumBoard> list= this.forumBoardService.getBoardTree(postType);
        return getSuccessResponseVO(list);
    }

    /**
     * @description: 发布文章
     * @author alj
     * @date 2024/4/12 10:15
     * @version 1.0
     */
    @RequestMapping("/postArticle")
    public ResponseVO postArticle(HttpSession session,
                                  String title,
                                  Integer boardId,
                                  Integer pBoardId,
                                  MultipartFile cover,
                                  String summary,
                                  Integer editorType,
                                  String content,
                                  String markdownContent){

        title= StringTools.convertHTML(title);
        SessionWebloginDto sessionWebloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        ForumArticle forumArticle=new ForumArticle();
        forumArticle.setPBoardId(pBoardId);
        forumArticle.setSummary(summary);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setContent(content);
        EditorTypeEnums typeEnums=EditorTypeEnums.getByType(editorType);
        if (typeEnums==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (EditorTypeEnums.MARKDOWN.getType().equals(editorType)&&StringTools.isEmpty(markdownContent)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setUserId(sessionWebloginDto.getUserId());
        forumArticle.setNickName(sessionWebloginDto.getNickName());
        forumArticleService.postArticle(forumArticle,cover,sessionWebloginDto.getAdmin());

        return getSuccessResponseVO(forumArticle.getArticleId());
    }

    /**
     * @description: 修改文章的详情
     * @author alj
     * @date 2024/4/12 14:41
     * @version 1.0
     */
    @RequestMapping("/articleDetail4Update")
    public ResponseVO articleDeatail4Update(HttpSession session, String articleId){
        SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        ForumArticle forumArticle=forumArticleService.getById(articleId);
        if (forumArticle==null||!forumArticle.getUserId().equals(webloginDto.getUserId())){
            throw new BusinessException("文章不存在或者你无权修改文章！");
        }
        ForumArticleDetailVO detailVO=new ForumArticleDetailVO();
        detailVO.setForumArticle(CopyTools.copy(forumArticle, ForumArticleVO.class));
        return getSuccessResponseVO(detailVO);
    }


    /**
     * @description: 更新文章
     * @author alj
     * @date 2024/4/12 15:00
     * @version 1.0
     */
    @RequestMapping("/updateArticle")
    public ResponseVO updateArticle(HttpSession session,
                                    String articleId,
                                    String title,
                                    Integer boardId,
                                    Integer pBoardId,
                                    MultipartFile cover,
                                    String summary,
                                    Integer editorType,
                                    String content,
                                    String markdownContent){
        title=StringTools.convertHTML(title);
        SessionWebloginDto webloginDto=(SessionWebloginDto) session.getAttribute("session_key");
        ForumArticle forumArticle= new ForumArticle();
        forumArticle.setArticleId(articleId);
        forumArticle.setPBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setContent(content);
        forumArticle.setUserId(webloginDto.getUserId());
        forumArticle.setSummary(summary);
        forumArticleService.updateArticle(webloginDto.getAdmin(),forumArticle,cover);
        return getSuccessResponseVO(forumArticle.getArticleId());
    }

    @RequestMapping("/search")
    public ResponseVO search(String keyword,Integer pageNo){
        QueryWrapper<ForumArticle> queryWrapper=new QueryWrapper<>();
        queryWrapper.like("title",keyword);
        Page<ForumArticle> pageinfo=new Page<>(pageNo==null||pageNo==0?1:pageNo,5);
        IPage<ForumArticle> pageResult=forumArticleMapper.selectPage(pageinfo,queryWrapper);
        PaginationResultVO<ForumArticle> resultVO = new PaginationResultVO<>(
                (int) pageResult.getTotal(), // 总记录数
                (int) pageResult.getSize(), // 每页记录数
                (int) pageResult.getCurrent(), // 当前页号
                (int) pageResult.getPages(), // 总页数
                pageResult.getRecords() // 当前页数据
        );
        return getSuccessResponseVO(resultVO);
    }

}
