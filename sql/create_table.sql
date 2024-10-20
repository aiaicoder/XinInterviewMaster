# 数据库初始化
# @author <a href="https://github.com/aiaicoder">程序员小新</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists mianShiTong;

-- 切换库
use mianShiTong;

-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment 'id' primary key,
    userAccount   varchar(256)                           not null comment '账号',
    userPassword  varchar(512)                           not null comment '密码',
    userName      varchar(256)                           null comment '用户昵称',
    userAvatar    varchar(1024)                          null comment '用户头像',
    userProfile   varchar(512)                           null comment '用户简介',
    userRole      varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    vipExpireTime datetime                               null comment '会员过期时间',
    vipCode       varchar(256)                           null comment '会员兑换码',
    vipNumber     bigint                                 null comment '会员编号',
    editTime      datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 题库表
create table if not exists question_bank
(
    id            bigint auto_increment comment 'id' primary key,
    title         varchar(256)                       null comment '标题',
    description   text                               null comment '描述',
    picture       varchar(2048)                      null comment '图片',
    userId        bigint                             not null comment '创建用户 id',
    priority      int      default 0                 null comment '优先级',
    reviewStatus  tinyint  default 0                 not null comment '审核状态（0-待审核，1-通过，2-拒绝）',
    reviewMessage varchar(512)                       null comment '审核信息',
    reviewUserId  bigint                             null comment '审核用户 id',
    reviewTime    datetime                           null comment '审核时间',
    editTime      datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    index idx_title (title)
) comment '题库' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(256)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '推荐答案',
    userId     bigint                             not null comment '创建用户 id',
    reviewStatus  tinyint  default 0                 not null comment '审核状态（0-待审核，1-通过，2-拒绝）',
    reviewMessage varchar(512)                       null comment '审核信息',
    reviewUserId  bigint                             null comment '审核用户 id',
    reviewTime    datetime                           null comment '审核时间',
    viewNum    int      default 0                 not null comment '浏览数',
    thumbNum   int      default 0                 not null comment '点赞数',
    needVip    tinyint  default 0                 not null comment '是否需要会员(1 表示仅会员可见)',
    priority   int      default 0                 null comment '优先级',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_title (title),
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;

-- 题库题目表（硬删除）
create table if not exists question_bank_question
(
    id             bigint auto_increment comment 'id' primary key,
    questionBankId bigint                             not null comment '题库 id',
    questionId     bigint                             not null comment '题目 id',
    userId         bigint                             not null comment '创建用户 id',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE (questionBankId, questionId)
) comment '题库题目' collate = utf8mb4_unicode_ci;