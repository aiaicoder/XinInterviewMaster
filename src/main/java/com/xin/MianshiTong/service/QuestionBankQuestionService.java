package com.xin.MianshiTong.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.xin.MianshiTong.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xin.MianshiTong.model.entity.QuestionBank;
import com.xin.MianshiTong.model.entity.QuestionBankQuestion;
import com.xin.MianshiTong.model.vo.QuestionBankQuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目题库服务
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    void validQuestionBank(QuestionBankQuestion questionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题目题库封装
     *
     * @param questionBankQuestion
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion);

    /**
     * 分页获取题目题库封装
     *
     * @param questionBankQuestionPage
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage);
}
