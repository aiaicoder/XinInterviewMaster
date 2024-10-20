package com.xin.MianshiTong.model.dto.QuestionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题目题库关联请求
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionBankQuestionEditRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}