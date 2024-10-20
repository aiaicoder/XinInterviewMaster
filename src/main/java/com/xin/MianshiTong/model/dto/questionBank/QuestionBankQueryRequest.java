package com.xin.MianshiTong.model.dto.questionBank;

import com.xin.MianshiTong.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询题库请求
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionBankQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

    private  Long userId;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 图片查询
     */
    private String picture;

    /**
     * 是否需要查询题目例表
     */
    private Boolean needQuestionList;

    private static final long serialVersionUID = 1L;
}