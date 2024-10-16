package com.xin.MianshiTong.controller;

import com.xin.MianshiTong.common.BaseResponse;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.common.ResultUtils;
import com.xin.MianshiTong.exception.BusinessException;
import com.xin.MianshiTong.model.dto.comment.CommentAddRequest;
import com.xin.MianshiTong.model.entity.Comment;
import com.xin.MianshiTong.model.entity.User;
import com.xin.MianshiTong.model.vo.CommentVO;
import com.xin.MianshiTong.service.CommentService;

import com.xin.MianshiTong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 */
@RestController
@RequestMapping("/question_comment")
@Slf4j
public class CommentsController {


    @Resource
    private UserService userService;

    @Resource
    private CommentService questionCommentService;

    // region 增删改查

    /**
     * 获取该问题的所有评论
     *
     * @param id
     * @return
     */
    @GetMapping("/getCommentList")
    public BaseResponse<List<CommentVO>> getCommentList(long id) {
        return ResultUtils.success(questionCommentService.getAllCommentList(id));
    }


    @PostMapping("/addComment")
    public BaseResponse<Boolean> addComment(@RequestBody Comment currentComment, @RequestBody(required = false) Comment parent) {
        User loginUser = userService.getLoginUser();
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }

    @PostMapping("wrap/addComment")
    public BaseResponse<Boolean> addCommentWrap(@RequestBody CommentAddRequest commentAddRequest) {
        User loginUser = userService.getLoginUser();
        Comment currentComment = commentAddRequest.getCurrentComment();
        Comment parent = commentAddRequest.getParentComment();
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }


    /**
     * 删除
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/deleteComment")
    public BaseResponse<Integer> deleteQuestion(@RequestBody Comment currentComment) {
        if (currentComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除空评论");
        }
        User loginUser = userService.getLoginUser();
        int updateCount = questionCommentService.deleteCommentById(currentComment, loginUser);
        return ResultUtils.success(updateCount);
    }

    /**
     * 更新（仅管理员）
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/updateLikeCount")
    public BaseResponse<Boolean> updateComment(@RequestBody Comment currentComment) {
        boolean updateLikeCount = questionCommentService.updateLikeCount(currentComment);
        return ResultUtils.success(updateLikeCount);
    }


}
