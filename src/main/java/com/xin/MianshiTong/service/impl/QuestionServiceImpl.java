package com.xin.MianshiTong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.MianshiTong.common.ErrorCode;
import com.xin.MianshiTong.constant.CommonConstant;
import com.xin.MianshiTong.exception.ThrowUtils;
import com.xin.MianshiTong.mapper.QuestionMapper;
import com.xin.MianshiTong.model.dto.post.PostEsDTO;
import com.xin.MianshiTong.model.dto.question.QuestionEsDTO;
import com.xin.MianshiTong.model.dto.question.QuestionQueryRequest;
import com.xin.MianshiTong.model.entity.Post;
import com.xin.MianshiTong.model.entity.Question;
import com.xin.MianshiTong.model.entity.QuestionBankQuestion;
import com.xin.MianshiTong.model.entity.User;
import com.xin.MianshiTong.model.vo.QuestionVO;
import com.xin.MianshiTong.model.vo.UserVO;
import com.xin.MianshiTong.service.QuestionBankQuestionService;
import com.xin.MianshiTong.service.QuestionService;
import com.xin.MianshiTong.service.UserService;
import com.xin.MianshiTong.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 *
 * @author <a href="https://github.com/aiaicoder">程序员小新</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        //todo 从对象中取值
        String title = question.getTitle();
        String content = question.getContent();
        String answer = question.getAnswer();
        String tags = question.getTags();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(answer), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(tags), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
            ThrowUtils.throwIf(content.length() > 1024, ErrorCode.PARAMS_ERROR, "内容过长");
            ThrowUtils.throwIf(answer.length() > 2024, ErrorCode.PARAMS_ERROR, "答案过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Integer needVip = questionQueryRequest.getNeedVip();
        Long userId = questionQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(needVip), "needVip", needVip);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
        });
        // endregion
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }


    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public Page<Question> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        //如果有bankId，则只返回bankId下的题目
        if (questionBankId != null) {
            //查询题库内的题目id
            LambdaQueryWrapper<QuestionBankQuestion> queryQbqWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                    .select(QuestionBankQuestion::getQuestionId);
            List<QuestionBankQuestion> questionIdList = questionBankQuestionService.list(queryQbqWrapper);
            Set<Long> questionIds = questionIdList.stream().map(QuestionBankQuestion::getQuestionId).collect(Collectors.toSet());
            if (CollUtil.isEmpty(questionIdList)) {
                queryWrapper.in("questionId", questionIds);
            }
        }
        // 查询数据库
        return this.page(new Page<>(current, size),
                queryWrapper);
    }

    /**
     * 从 ES 查询题目
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public Page<Question> searchFromEs(QuestionQueryRequest questionQueryRequest) {
        // 获取参数
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String searchText = questionQueryRequest.getSearchText();
        List<String> tags = questionQueryRequest.getTags();
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        Long userId = questionQueryRequest.getUserId();
        // 注意，ES 的起始页为 0
        int current = questionQueryRequest.getCurrent() - 1;
        int pageSize = questionQueryRequest.getPageSize();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 构造查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        if (questionBankId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("questionBankId", questionBankId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            // title = '' or content = '' or answer = ''
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("answer", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of(current, pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSorts(sortBuilder)
                .build();
        SearchHits<QuestionEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, QuestionEsDTO.class);
        // 复用 MySQL / MyBatis Plus 的分页对象，封装返回结果
        Page<Question> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Question> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<QuestionEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> questionIdList = searchHitList.stream().
                    map(searchHit -> searchHit.getContent().getId()).collect(Collectors.toList());
            List<Question> questions = listByIds(questionIdList);
            if (questions != null) {
                Map<Long, List<Question>> idQuestionMap = questions.stream().collect(Collectors.groupingBy(Question::getId));
                questionIdList.forEach(questionId -> {
                    if (idQuestionMap.containsKey(questionId)) {
                        resourceList.add(idQuestionMap.get(questionId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(questionId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

}

