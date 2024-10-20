package com.xin.MianshiTong.service.impl;
import java.util.Date;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.constant.CommonConstant;
import com.xin.MianshiTong.exception.ThrowUtils;
import com.xin.MianshiTong.mapper.QuestionBankMapper;
import com.xin.MianshiTong.model.dto.questionBank.QuestionBankQueryRequest;
import com.xin.MianshiTong.model.entity.QuestionBank;
import com.xin.MianshiTong.model.entity.User;
import com.xin.MianshiTong.model.vo.QuestionBankVO;
import com.xin.MianshiTong.model.vo.UserVO;
import com.xin.MianshiTong.service.QuestionBankService;
import com.xin.MianshiTong.service.UserService;
import com.xin.MianshiTong.utils.SqlUtils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 题库服务实现
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param questionBank
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBank(QuestionBank questionBank, boolean add) {
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR);
        String title = questionBank.getTitle();
        String description = questionBank.getDescription();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(description), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
            ThrowUtils.throwIf(description.length() > 1024, ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQueryRequest.getId();
        Long notId = questionBankQueryRequest.getNotId();
        String title = questionBankQueryRequest.getTitle();
        String searchText = questionBankQueryRequest.getSearchText();
        String sortField = questionBankQueryRequest.getSortField();
        String sortOrder = questionBankQueryRequest.getSortOrder();
        Long userId = questionBankQueryRequest.getUserId();
        String description = questionBankQueryRequest.getDescription();
        Integer priority = questionBankQueryRequest.getPriority();
        String picture = questionBankQueryRequest.getPicture();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(priority), "priority", priority);
        queryWrapper.eq(StringUtils.isNotBlank(picture), "picture", picture);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库封装
     *
     * @param questionBank
     * @return
     */
    @Override
    public QuestionBankVO getQuestionBankVO(QuestionBank questionBank) {
        // 对象转封装类
        QuestionBankVO questionBankVO = QuestionBankVO.objToVo(questionBank);
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBank.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankVO.setUser(userVO);
        return questionBankVO;
    }

    /**
     * 分页获取题库封装
     *
     * @param questionBankPage
     * @return
     */
    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage) {
        List<QuestionBank> questionBankList = questionBankPage.getRecords();
        Page<QuestionBankVO> questionBankVOPage = new Page<>(questionBankPage.getCurrent(), questionBankPage.getSize(), questionBankPage.getTotal());
        if (CollUtil.isEmpty(questionBankList)) {
            return questionBankVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankVO> questionBankVOList = questionBankList.stream().map(QuestionBankVO::objToVo).collect(Collectors.toList());
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankList.stream().map(QuestionBank::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        questionBankVOList.forEach(questionBankVO -> {
            Long userId = questionBankVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankVO.setUser(userService.getUserVO(user));
        });
        questionBankVOPage.setRecords(questionBankVOList);
        return questionBankVOPage;
    }

}
