package com.xin.MianshiTong.esdao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/10/20 19:19
 */
@SpringBootTest
class QuestionEsDaoTest {

    @Resource
    private QuestionEsDao questionEsDao;

    @Test
    void findByUserId() {
        questionEsDao.findByUserId(12345L);
    }
}