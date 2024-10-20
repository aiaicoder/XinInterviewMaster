package com.xin.MianshiTong.esdao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.xin.MianshiTong.model.entity.Question;

import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.Date;

@SpringBootTest
public class QuestionElasticsearchTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
public void testCreate() throws JsonProcessingException {
    String indexName = "question_v1";
    String documentId = "1";

    // Create
    Question question = new Question();
    question.setId(Long.valueOf(documentId));
    question.setTitle("Sample Question");
    question.setContent("This is a sample content.");
    question.setTags("[\"sample\", \"test\"]");
    question.setAnswer("Sample answer");
    question.setUserId(12345L);
    question.setReviewStatus(0);
    question.setReviewMessage("Pending review");
    question.setReviewUserId(54321L);
    question.setReviewTime(new Date());
    question.setViewNum(100);
    question.setThumbNum(10);
    question.setNeedVip(0);
    question.setPriority(1);
    question.setEditTime(new Date());
    question.setCreateTime(new Date());
    question.setUpdateTime(new Date());
    question.setIsDelete(0);

    // 使用 ObjectMapper 进行序列化
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

    String json = objectMapper.writeValueAsString(question);

    IndexQuery indexQuery = new IndexQueryBuilder()
            .withId(documentId)
            .withSource(json)
            .build();
    elasticsearchRestTemplate.index(indexQuery, IndexCoordinates.of(indexName));
}

    @Test
    public void testRead() {
        String indexName = "question";
        String documentId = "1";
        // Read
        Question retrievedQuestion = elasticsearchRestTemplate.get(documentId, Question.class, IndexCoordinates.of(indexName));
        if (retrievedQuestion != null) {
            System.out.println("Retrieved Question: " + retrievedQuestion.getTitle());
        }
    }

    @Test
    public void testUpdate() {
        String indexName = "question_v1";
        String documentId = "1";

        // Update
        UpdateQuery updateQuery = UpdateQuery.builder(documentId)
                .withDocument(Document.parse("{\"content\":\"This is updated content.\"}"))
                .build();
        elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of(indexName));
    }

    @Test
    public void testDelete() {
        String indexName = "question_v1";
        String documentId = "1";
        // Delete
        elasticsearchRestTemplate.delete(documentId, IndexCoordinates.of(indexName));
    }
}