package com.xin.MianshiTong.esdao;

import com.xin.MianshiTong.model.dto.post.PostEsDTO;
import com.xin.MianshiTong.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    List<QuestionEsDTO> findByUserId(Long userId);
}