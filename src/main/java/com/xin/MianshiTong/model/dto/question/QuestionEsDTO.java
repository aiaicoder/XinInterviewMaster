package com.xin.MianshiTong.model.dto.question;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xin.MianshiTong.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Elasticsearch DTO for Question
 *
 * @author xin
 */
@Document(indexName = "question")
@Data
public class QuestionEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    @Field(type = FieldType.Text)
    private String title;

    /**
     * 内容
     */
    @Field(type = FieldType.Text)
    private String content;

    /**
     * 答案
     */
    @Field(type = FieldType.Text)
    private String answer;

    /**
     * 标签列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 创建用户 id
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 审核状态
     */
    @Field(type = FieldType.Integer)
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @Field(type = FieldType.Text)
    private String reviewMessage;

    /**
     * 审核用户 id
     */
    @Field(type = FieldType.Long)
    private Long reviewUserId;

    /**
     * 审核时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date reviewTime;

    /**
     * 是否需要会员
     */
    @Field(type = FieldType.Keyword)
    private Integer needVip;

    /**
     * 优先级
     */
    @Field(type = FieldType.Integer)
    private Integer priority;

    /**
     * 编辑时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date editTime;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    @Field(type = FieldType.Keyword)
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionEsDTO objToDto(Question question) {
        if (question == null) {
            return null;
        }
        QuestionEsDTO questionEsDTO = new QuestionEsDTO();
        BeanUtils.copyProperties(question, questionEsDTO);
        String tagsStr = question.getTags();
        if (StrUtil.isNotBlank(tagsStr)) {
            questionEsDTO.setTags(JSONUtil.toList(JSONUtil.parseArray(tagsStr), String.class));
        }
        return questionEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param questionEsDTO
     * @return
     */
    public static Question dtoToObj(QuestionEsDTO questionEsDTO) {
        if (questionEsDTO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEsDTO, question);
        List<String> tagList = questionEsDTO.getTags();
        if (CollUtil.isNotEmpty(tagList)) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        return question;
    }
}