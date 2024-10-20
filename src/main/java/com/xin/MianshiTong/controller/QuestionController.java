package com.xin.MianshiTong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.MianshiTong.common.BaseResponse;
import com.xin.MianshiTong.common.DeleteRequest;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.common.ResultUtils;
import com.xin.MianshiTong.constant.UserConstant;
import com.xin.MianshiTong.exception.BusinessException;
import com.xin.MianshiTong.exception.ThrowUtils;
import com.xin.MianshiTong.model.dto.question.QuestionAddRequest;
import com.xin.MianshiTong.model.dto.question.QuestionEditRequest;
import com.xin.MianshiTong.model.dto.question.QuestionQueryRequest;
import com.xin.MianshiTong.model.dto.question.QuestionUpdateRequest;
import com.xin.MianshiTong.model.entity.Question;
import com.xin.MianshiTong.model.entity.QuestionBank;
import com.xin.MianshiTong.model.entity.QuestionBankQuestion;
import com.xin.MianshiTong.model.entity.User;
import com.xin.MianshiTong.model.enums.UserRoleEnum;
import com.xin.MianshiTong.model.vo.QuestionVO;
import com.xin.MianshiTong.service.QuestionBankQuestionService;
import com.xin.MianshiTong.service.QuestionService;
import com.xin.MianshiTong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;



    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole({UserConstant.ADMIN_ROLE, UserConstant.VIP_ROLE})
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        List<String> tags = questionAddRequest.getTags();

        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        if (tags != null) {
            String tagsString = JSONUtil.toJsonStr(tags);
            question.setTags(tagsString);
        }
        // 数据校验
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser();
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @SaCheckRole({UserConstant.ADMIN_ROLE, UserConstant.VIP_ROLE})
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        checkRole(user, oldQuestion);
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole({UserConstant.ADMIN_ROLE, UserConstant.VIP_ROLE})
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            String tagsString = JSONUtil.toJsonStr(tags);
            question.setTags(tagsString);
        }
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVoById(long id) {
        User loginUser = userService.getLoginUserNe();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        if (loginUser == null || (question.getNeedVip() == 1 && !loginUser.getUserRole().equals(UserRoleEnum.VIP.getValue()))) {
            // 只展示部分内容
            String partialAnswer = getPartialAnswer(question.getAnswer());
            question.setAnswer(partialAnswer);
            // 其他需要展示的字段
            return ResultUtils.success(questionService.getQuestionVO(question));
        }
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question));
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage));
    }

    /**
     * 分页获取当前登录用户创建的题目列表,仅限管理员和会员
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @SaCheckRole({UserConstant.ADMIN_ROLE, UserConstant.VIP_ROLE})
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser();
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage));
    }

    /**
     * 编辑题目
     *
     * @param questionEditRequest
     * @return
     */
    @PostMapping("/edit")
    @SaCheckRole({UserConstant.ADMIN_ROLE, UserConstant.VIP_ROLE})
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            String tagsString = JSONUtil.toJsonStr(tags);
            question.setTags(tagsString);
        }
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        checkRole(loginUser, oldQuestion);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/search/page/vo")
    public BaseResponse<Page<QuestionVO>> searchQuestionVoByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询 ES
         Page<Question> questionPage = questionService.searchFromEs(questionQueryRequest);
        // 查询数据库（作为没有 ES 的降级方案）
//        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage));
    }

    private void checkRole(User loginUser, Question oldQuestion) {
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    //只返回部分内容
    private String getPartialAnswer(String answer) {
        if (answer != null && answer.length() > 20) {
            return answer.substring(0, 20) + "..."; // 只展示前20个字符
        }
        return answer;
    }
}
