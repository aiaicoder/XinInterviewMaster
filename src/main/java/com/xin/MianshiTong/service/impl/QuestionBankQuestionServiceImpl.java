package com.xin.MianshiTong.service.impl;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.exception.ThrowUtils;
import com.xin.MianshiTong.mapper.QuestionBankQuestionMapper;
import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xin.MianshiTong.model.entity.Question;
import com.xin.MianshiTong.model.entity.QuestionBank;
import com.xin.MianshiTong.model.entity.QuestionBankQuestion;
import com.xin.MianshiTong.model.vo.QuestionBankQuestionVO;
import com.xin.MianshiTong.service.QuestionBankQuestionService;
import com.xin.MianshiTong.service.QuestionBankService;
import com.xin.MianshiTong.service.QuestionService;
import com.xin.MianshiTong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题目题库服务实现
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;


    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBank(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        //题目和题库必须存在
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId !=null){
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        if (questionBankId !=null){
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");
        }
    }


    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQuestionQueryRequest.getId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        return queryWrapper;
    }

    /**
     * 获取题目题库封装
     *
     * @param questionBankQuestion
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion) {
        // 对象转封装类
        return QuestionBankQuestionVO.objToVo(questionBankQuestion);
    }

    /**
     * 分页获取题目题库封装
     *
     * @param questionBankQuestionPage
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        return questionBankQuestionVOPage;
    }

}
