
CREATE DATABASE  IF NOT EXISTS  `hmily`  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;

USE `hmily`;

CREATE TABLE IF NOT EXISTS `hmily_lock`
(
    `lock_id`           varchar(128) not null comment '主键id',
    `trans_id`          varchar(128) not null comment '全局事务id',
    `participant_id`    varchar(128) not null comment 'hmily参与者id',
    `resource_id`       varchar(256) not null comment '资源id',
    `target_table_name` varchar(64)  not null comment '锁定目标表名',
    `target_table_pk`   varchar(64)  not null comment '锁定表主键',
    `create_time`       datetime     not null comment '创建时间',
    `update_time`       datetime     not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    constraint hmily_lock_lock_id_uindex  unique (lock_id)
) comment 'hmily全局lock表';

alter table hmily_lock add primary key (lock_id);


create table if not exists `hmily_participant_undo`
(
    `undo_id`         varchar(128) not null comment '主键id',
    `participant_id`   varchar(128) not null comment '参与者id',
    `trans_id`        varchar(128) not null comment '全局事务id',
    `resource_id`     varchar(256) not null comment '资源id，at模式下为jdbc url',
    `undo_invocation` varbinary             comment '回滚调用点',
    `status`          int(2)      not null comment '状态',
    `create_time`     datetime     not null comment '创建时间',
    `update_time`     datetime     not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    constraint hmily_partcipant_undo_undo_id_uindex  unique (undo_id)
) comment 'hmily事务参与者undo记录，用在AC模式';

alter table hmily_participant_undo   add primary key (undo_id);


create table if not exists `hmily_transaction_global`
(
    `trans_id`    varchar(128)  not null comment '全局事务id',
    `app_name`    varchar(128)  not null comment '应用名称',
    `status`      int(2)       not null comment '事务状态',
    `trans_type`  varchar(16)   not null comment '事务模式',
    `retry`       int(2) default 0 not null comment '重试次数',
    `version`     int(2)           not null comment '版本号',
    `create_time` datetime     not null comment '创建时间',
    `update_time` datetime     not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    constraint hmily_transaction_trans_id_uindex  unique (trans_id)
) comment 'hmily事务表（发起者）';

alter table hmily_transaction_global add primary key (trans_id);

create table if not exists `hmily_transaction_participant`
(
    `participant_id`     varchar(128)  not null comment '参与者事务id',
    `participant_ref_id` varchar(128)  not null comment '参与者关联id且套调用时候会存在',
    `trans_id`           varchar(128)  not null comment '全局事务id',
    `trans_type`         varchar(16)   not null comment '事务类型',
    `status`             int(2)       not null comment '分支事务状态',
    `app_ame`            varchar(64)   not null comment '应用名称',
    `role`               int(2)       not null comment '事务角色',
    `retry`              int(2) default 0 not null comment '重试次数',
    `target_class`        varchar(512)  null comment '接口名称',
    `target_method`      varchar(128)  null comment '接口方法名称',
    `confirm_method`     varchar(128)  null comment 'confirm方法名称',
    `cancel_method`      varchar(128)  null comment 'cancel方法名称',
    `confirm_invocation` varbinary          comment 'confirm调用点',
    `cancel_invocation`  varbinary          comment 'cancel调用点',
    `version`            int(2) default 0 not null,
    `create_time`        datetime     not null comment '创建时间',
    `update_time`        datetime     not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    constraint hmily_participant_participant_Id_uindex unique (participant_id)
) comment 'hmily事务参与者';

alter table hmily_transaction_participant add primary key (participant_id);


