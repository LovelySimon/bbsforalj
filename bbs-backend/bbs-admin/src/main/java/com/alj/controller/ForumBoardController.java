package com.alj.controller;

import com.alj.dto.FileUploadDto;
import com.alj.enums.FileUpLoadTypeEnum;
import com.alj.mapper.ForumBoardMapper;
import com.alj.pojo.ForumBoard;
import com.alj.service.ForumBoardService;
import com.alj.util.FileUtil;
import com.alj.vo.ResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/board")
public class ForumBoardController extends ABaseController{
    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private FileUtil fileUtil;

    @Resource
    private ForumBoardMapper forumBoardMapper;

    /**
     * @description: 加载板块信息
     * @author alj
     * @date 2024/4/16 9:54
     * @version 1.0
     */
    @RequestMapping("/loadBoard")
    public ResponseVO loadBoard(){
        return getSuccessResponseVO(forumBoardService.getBoardTree(null));
    }

    /**
     * @description: 更改或新增板块信息
     * @author alj
     * @date 2024/4/16 9:54
     * @version 1.0
     */
    @RequestMapping("/saveBoard")
    public ResponseVO saveBoard(Integer boardId, Integer pBoardId, String boardName, String boardDesc, Integer postType, MultipartFile cover){
        ForumBoard forumBoard=new ForumBoard();
        forumBoard.setBoardId(boardId);
        forumBoard.setpBoardId(pBoardId);
        forumBoard.setBoardName(boardName);
        forumBoard.setBoardDesc(boardDesc);
        forumBoard.setPostType(postType);
        if (cover!=null){
            FileUploadDto fileUploadDto=fileUtil.uploadFile2local(cover,"/images/", FileUpLoadTypeEnum.ARTICLE_COVER);
            forumBoard.setCover(fileUploadDto.getLocalPath());
        }
        forumBoardService.saveForumBoard(forumBoard);
        return getSuccessResponseVO(null);
    }


    /**
     * @description: 删除板块
     * @author alj
     * @date 2024/4/16 10:30
     * @version 1.0
     */
    @RequestMapping("/delBoard")
    public ResponseVO delBoard(Integer boardId){
        this.forumBoardMapper.delete(new QueryWrapper<ForumBoard>().eq("board_id",boardId));
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 改变板块的排序
     * @author alj
     * @date 2024/4/16 10:47
     * @version 1.0
     */
    @RequestMapping("/changeBoardSort")
    public ResponseVO changeBoardSort(String boardIds){
        forumBoardService.changeSort(boardIds);
        return getSuccessResponseVO(null);
    }
}
