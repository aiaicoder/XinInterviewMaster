package com.xin.MianshiTong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.MianshiTong.common.BaseResponse;
import com.xin.MianshiTong.common.DeleteRequest;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.common.ResultUtils;
import com.xin.MianshiTong.constant.UserConstant;
import com.xin.MianshiTong.exception.BusinessException;
import com.xin.MianshiTong.exception.ThrowUtils;
import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionAddRequest;
import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionRemoveRequest;
import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionUpdateRequest;
import com.xin.MianshiTong.model.entity.QuestionBankQuestion;
import com.xin.MianshiTong.model.entity.User;
import com.xin.MianshiTong.model.vo.QuestionBankQuestionVO;
import com.xin.MianshiTong.service.QuestionBankQuestionService;
import com.xin.MianshiTong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 题目题库接口
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/questionBankQuestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题目题库
     *
     * @param questionBankQuestionAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBankQuestion(@RequestBody QuestionBankQuestionAddRequest questionBankQuestionAddRequest) {
        ThrowUtils.throwIf(questionBankQuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionBankQuestionAddRequest, questionBankQuestion);
        // todo 填充默认值
        User loginUser = userService.getLoginUser();
        questionBankQuestion.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionBankQuestionService.save(questionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankQuestionId = questionBankQuestion.getId();
        return ResultUtils.success(newQuestionBankQuestionId);
    }

    /**
     * 删除题目题库
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionBankQuestion(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBankQuestion oldQuestionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBankQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionBankQuestionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目题库（仅管理员可用）
     *
     * @param questionBankQuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBankQuestion(@RequestBody QuestionBankQuestionUpdateRequest questionBankQuestionUpdateRequest) {
        if (questionBankQuestionUpdateRequest == null || questionBankQuestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionBankQuestionUpdateRequest, questionBankQuestion);
        // 判断是否存在
        long id = questionBankQuestionUpdateRequest.getId();
        QuestionBankQuestion oldQuestionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionBankQuestionService.updateById(questionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目题库（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankQuestionVO> getQuestionBankQuestionVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionBankQuestion questionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVO(questionBankQuestion));
    }

    /**
     * 分页获取题目题库列表（仅管理员可用）
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionBankQuestionByPage(@RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBankQuestion> questionBankQuestionPage = questionBankQuestionService.page(new Page<>(current, size),
                questionBankQuestionService.getQueryWrapper(questionBankQuestionQueryRequest));
        return ResultUtils.success(questionBankQuestionPage);
    }

    /**
     * 分页获取题目题库列表（封装类）
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listQuestionBankQuestionVOByPage(@RequestBody QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        long current = questionBankQuestionQueryRequest.getCurrent();
        long size = questionBankQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> questionBankQuestionPage = questionBankQuestionService.page(new Page<>(current, size),
                questionBankQuestionService.getQueryWrapper(questionBankQuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVOPage(questionBankQuestionPage));
    }

    /**
     * 删除题库
     *
     * @return
     */
    @PostMapping("/remove")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeQuestionBank(@RequestBody QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest) {
        ThrowUtils.throwIf(questionBankQuestionRemoveRequest == null || questionBankQuestionRemoveRequest.getQuestionBankId() <= 0 || questionBankQuestionRemoveRequest.getQuestionId() <= 0, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestionRemoveRequest.getQuestionBankId();
        Long questionId = questionBankQuestionRemoveRequest.getQuestionId();

        LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class).
                eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .eq(QuestionBankQuestion::getQuestionId, questionId);
        boolean result = questionBankQuestionService.remove(queryWrapper);
        return ResultUtils.success(result);
    }

}
