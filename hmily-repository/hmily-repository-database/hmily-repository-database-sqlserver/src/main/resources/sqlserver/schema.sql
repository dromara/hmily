IF NOT EXISTS(SELECT * FROM  sys.databases  WHERE name = 'hmily' )
    CREATE DATABASE hmily ;
/
use hmily;
-- -----------------------------------------
-- create table hmily_lock if not exist ----
-- -----------------------------------------
IF NOT EXISTS(SELECT * FROM sysobjects WHERE name = 'hmily_lock' )
BEGIN
CREATE TABLE  hmily_lock (
    trans_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    resource_id VARCHAR(256) NOT NULL,
    target_table_name VARCHAR(64) NOT NULL,
    target_table_pk VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (resource_id, target_table_name, target_table_pk)
    );
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'全局事务id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'trans_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'hmily参与者id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'participant_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'资源id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'resource_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'锁定目标表名' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'target_table_name';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'锁定表主键' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'target_table_pk';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'创建时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'create_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'更新时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock', @level2type=N'COLUMN',@level2name=N'update_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'hmily全局lock表' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_lock';
END
-- ----------------------------------------------------
-- create table hmily_participant_undo if not exist ---
-- ----------------------------------------------------
IF NOT EXISTS(SELECT * FROM sysobjects WHERE name = 'hmily_participant_undo' )
BEGIN
CREATE TABLE hmily_participant_undo (
    undo_id BIGINT  NOT NULL PRIMARY KEY,
    participant_id BIGINT  NOT NULL ,
    trans_id BIGINT  NOT NULL ,
    resource_id VARCHAR(256 )  NOT NULL ,
    data_snapshot VARBINARY(MAX)  NOT NULL ,
    status INT  NOT NULL ,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
     );
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'主键id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'undo_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'参与者id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'participant_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'全局事务id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'trans_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'资源id，at模式下为jdbc url' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'resource_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'回滚数据快照' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN', @level2name=N'data_snapshot';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'状态' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'status';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'创建时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'create_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'更新时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo', @level2type=N'COLUMN',@level2name=N'update_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'hmily事务参与者undo记录，用在AC模式' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_participant_undo';
END
-- ------------------------------------------------------
-- create table hmily_transaction_global if not exist ---
-- ------------------------------------------------------
IF NOT EXISTS(SELECT * FROM sysobjects WHERE name = 'hmily_transaction_global' )
BEGIN
CREATE TABLE hmily_transaction_global (
    trans_id BIGINT  NOT NULL PRIMARY KEY,
    app_name VARCHAR(128 )  NOT NULL ,
    status INT  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    retry INT  DEFAULT 0  NOT NULL ,
    version INT  NOT NULL ,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'全局事务id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'trans_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'应用名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'app_name';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'事务状态' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'status';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'事务模式' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'trans_type';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'重试次数' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'retry';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'版本号' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'version';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'创建时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'create_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'更新时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global', @level2type=N'COLUMN',@level2name=N'update_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'hmily事务表（发起者）' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_global';
END
-- -----------------------------------------------------------
-- create table hmily_transaction_participant if not exist ---
-- -----------------------------------------------------------
IF NOT EXISTS(SELECT * FROM sysobjects WHERE name = 'hmily_transaction_participant' )
BEGIN
CREATE TABLE hmily_transaction_participant (
    participant_id BIGINT  NOT NULL PRIMARY KEY,
    participant_ref_id BIGINT  ,
    trans_id BIGINT  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    status INT  NOT NULL ,
    app_name VARCHAR(64 )  NOT NULL ,
    role INT  NOT NULL ,
    retry INT  DEFAULT 0  NOT NULL ,
    target_class VARCHAR(512 )  NULL ,
    target_method VARCHAR(128 )  NULL ,
    confirm_method VARCHAR(128 )  NULL ,
    cancel_method VARCHAR(128 )  NULL ,
    confirm_invocation VARBINARY(MAX)  NULL ,
    cancel_invocation VARBINARY(MAX)  NULL ,
    version INT DEFAULT 0 NOT NULL ,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'参与者事务id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'participant_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'参与者关联id且套调用时候会存在' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'participant_ref_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'全局事务id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'trans_id';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'事务类型' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'trans_type';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'分支事务状态' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'status';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'应用名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'app_name';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'事务角色' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'role';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'重试次数' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'retry';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'接口名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'target_class';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'接口方法名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'target_method';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'confirm方法名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'confirm_method';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'cancel方法名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'cancel_method';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'confirm调用点' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'confirm_invocation';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'cancel调用点' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'cancel_invocation';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'创建时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'create_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'更新时间' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant', @level2type=N'COLUMN',@level2name=N'update_time';
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'hmily事务参与者' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'hmily_transaction_participant';
END

/
-- ---------------------------------------------------------
-- Triggers structure for table hmily_lock auto_update------
-- ---------------------------------------------------------
CREATE TRIGGER hmily_lock_trigger
ON hmily_lock
AFTER UPDATE AS
BEGIN
	SET NOCOUNT ON;
	UPDATE hmily_lock
	SET update_time=SYSDATETIME()
	WHERE lock_id IN (SELECT DISTINCT lock_id FROM inserted)
END
/
ALTER TABLE hmily_lock ENABLE TRIGGER hmily_lock_trigger
/
-- ---------------------------------------------------------------------
-- Triggers structure for table hmily_participant_undo auto_update------
-- ---------------------------------------------------------------------
CREATE TRIGGER hmily_participant_undo_tigger
ON hmily_participant_undo
AFTER UPDATE AS
BEGIN
	SET NOCOUNT ON;
	UPDATE hmily_participant_undo
	SET update_time=SYSDATETIME()
	WHERE undo_id IN (SELECT DISTINCT undo_id FROM inserted)
END
/
ALTER TABLE hmily_participant_undo ENABLE TRIGGER hmily_participant_undo_tigger
/
-- --------------------------------------------------------------------
-- Triggers structure for table hmily_transaction_global auto_update---
-- --------------------------------------------------------------------
CREATE TRIGGER hmily_global_tigger
ON hmily_transaction_global
AFTER UPDATE AS
BEGIN
	SET NOCOUNT ON;
	UPDATE hmily_transaction_global
	SET update_time=SYSDATETIME()
	WHERE trans_id IN (SELECT DISTINCT trans_id FROM inserted)
END
/
ALTER TABLE hmily_transaction_global ENABLE TRIGGER hmily_global_tigger
/
-- -------------------------------------------------------------------------
-- Triggers structure for table hmily_transaction_participant auto_update---
-- -------------------------------------------------------------------------
CREATE TRIGGER hmily_participant_tigger
ON hmily_transaction_participant
AFTER UPDATE AS
BEGIN
	SET NOCOUNT ON;
	UPDATE hmily_transaction_participant
	SET update_time=SYSDATETIME()
	WHERE participant_id IN (SELECT DISTINCT participant_id FROM inserted)
END
/
ALTER TABLE hmily_transaction_participant ENABLE TRIGGER hmily_participant_tigger
/
