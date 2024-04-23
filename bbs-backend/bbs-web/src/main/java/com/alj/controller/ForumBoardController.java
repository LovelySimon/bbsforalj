package com.alj.controller;

import com.alj.service.ForumBoardService;
import com.alj.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/board")
public class ForumBoardController extends ABaseController {
    @Resource
    private ForumBoardService forumBoardService;

    /**
     * @description: 0高权限展示1普通权限展示
     * @author alj
     * @date 2024/4/4 16:24
     * @version 1.0
     */
    @RequestMapping("/loadBoard")
    public ResponseVO loadBoard(){
        return getSuccessResponseVO(forumBoardService.getBoardTree(1));
    }
}
