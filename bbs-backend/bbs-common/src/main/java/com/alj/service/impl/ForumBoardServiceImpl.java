package com.alj.service.impl;

import com.alj.exception.BusinessException;
import com.alj.mapper.ForumArticleMapper;
import com.alj.pojo.ForumBoard;
import com.alj.mapper.ForumBoardMapper;
import com.alj.service.ForumBoardService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 文章板块信息 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class ForumBoardServiceImpl extends ServiceImpl<ForumBoardMapper, ForumBoard> implements ForumBoardService {
    @Resource
    private ForumBoardMapper forumBoardMapper;

    @Resource
    private ForumArticleMapper forumArticleMapper;
    /**
     * @description: 获取板块列表
     * @author alj
     * @date 2024/4/4 15:47
     * @version 1.0
     */
    @Override
    public List<ForumBoard> getBoardTree(Integer postType) {
        List<ForumBoard> forumBoards=null;
        if (postType==null){
            QueryWrapper<ForumBoard> queryWrapper=new QueryWrapper<>();
            queryWrapper.orderByAsc("sort");
            forumBoards=this.forumBoardMapper.selectList(queryWrapper);
        }
        if (postType != null) {
            QueryWrapper<ForumBoard> queryWrapper=new QueryWrapper<>();
            queryWrapper.orderByAsc("sort");
            queryWrapper.eq("post_type",postType);
            forumBoards=this.forumBoardMapper.selectList(queryWrapper);
        }
        return convert(forumBoards,0);
    }

    /**
     * @description: 树形表示
     * @author alj
     * @date 2024/4/4 15:46
     * @version 1.0
     */
    private List<ForumBoard> convert(List<ForumBoard> dataList,Integer pid){
        List<ForumBoard> children = new ArrayList<>();
        for (ForumBoard m:dataList){
            if (m.getpBoardId().equals(pid)){
                m.setChildren(convert(dataList,m.getBoardId()));
                children.add(m);
            }
        }
        return children;
    }


    /**
     * @description: 保存修改的板块信息
     * @author alj
     * @date 2024/4/16 10:10
     * @version 1.0
     */
    @Override
    public void saveForumBoard(ForumBoard forumBoard) {
        if (forumBoard.getBoardId()==null){
            QueryWrapper<ForumBoard> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("p_board_id",forumBoard.getpBoardId());
            Integer count=this.forumBoardMapper.selectCount(queryWrapper);
            forumBoard.setSort(count+1);
            this.forumBoardMapper.insert(forumBoard);
        }else{
            ForumBoard dbinfo=this.forumBoardMapper.selectOne(new QueryWrapper<ForumBoard>().eq("board_id",forumBoard.getBoardId()));
            if (dbinfo==null){
                throw new BusinessException("板块信息不存在");
            }
            UpdateWrapper<ForumBoard> updateWrapper=new UpdateWrapper<>();
            updateWrapper.eq("board_id",forumBoard.getBoardId());
            this.forumBoardMapper.update(forumBoard,updateWrapper);
            if (!dbinfo.getBoardName().equals(forumBoard.getBoardName())){
                forumArticleMapper.updateBoardNameBatch(dbinfo.getpBoardId()==0?0:1,forumBoard.getBoardName(),forumBoard.getBoardId());
            }
        }

    }

    /**
     * @description: 改变排序
     * @author alj
     * @date 2024/4/16 10:45
     * @version 1.0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeSort(String boardIds) {
        String[] boardIdArray=boardIds.split(",");
        Integer index=1;
        for(String boardIdstr:boardIdArray){
            Integer boardId=Integer.parseInt(boardIdstr);
            ForumBoard board=new ForumBoard();
            board.setSort(index);
            forumBoardMapper.update(board,new UpdateWrapper<ForumBoard>().eq("board_id",boardId));
            index++;
        }
    }


}
