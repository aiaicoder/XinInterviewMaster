package com.xin.MianshiTong.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xin.MianshiTong.model.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
* @author 15712
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-10-06 20:42:22
* @Entity generator.domain.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {
    List<Question> listQuestionWithDelete(@Param("fiveMinutesAgoDate") Date fiveMinutesAgoDate);

}




