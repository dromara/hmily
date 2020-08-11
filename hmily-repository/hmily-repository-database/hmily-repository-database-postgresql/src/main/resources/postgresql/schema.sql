-- -----------------------------------------------
-- init a trigger for auto update upadate_time ---
-- -----------------------------------------------
CREATE OR REPLACE FUNCTION update_timestamp() RETURNS TRIGGER AS
$$
BEGIN
    NEW.update_time = current_timestamp;
    RETURN NEW;
END
$$
language plpgsql;
-- ---------------------------------------------
-- init postgresql hmily_transaction  tables ---
-- ---------------------------------------------
DO
$do$
BEGIN
-- -----------------------------------------
-- create table hmily_lock if not exist ----
-- -----------------------------------------
IF (SELECT COUNT(1) FROM pg_class  WHERE relname  = 'hmily_lock') > 0 THEN
    RAISE NOTICE 'hmily_lock already exists';
ELSE
    EXECUTE ' CREATE TABLE  hmily_lock (
    lock_id INT8 NOT NULL PRIMARY KEY ,
    trans_id INT8 NOT NULL,
    participant_id INT8 NOT NULL,
    resource_id VARCHAR(256) NOT NULL,
    target_table_name VARCHAR(64) NOT NULL,
    target_table_pk VARCHAR(64) NOT NULL,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
    )';

    EXECUTE ' COMMENT ON TABLE hmily_lock IS ''' ||'hmily全局lock表' || '''';
    EXECUTE ' COMMENT ON COLUMN hmily_lock.lock_id IS ''' ||'主键id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.trans_id  IS ''' ||'全局事务id' || '''';
    EXECUTE ' COMMENT ON COLUMN hmily_lock.participant_id IS ''' ||'hmily参与者id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.resource_id IS ''' ||'资源id'|| '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.target_table_name IS ''' ||'锁定目标表名' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.target_table_pk IS ''' ||'锁定表主键' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.create_time IS ''' ||'创建时间' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_lock.update_time IS ''' ||'更新时间' || '''';
	-- ------------------------------------------
    -- Triggers structure for table hmily_lock---
    -- ------------------------------------------
	EXECUTE ' CREATE TRIGGER hmily_lock_trigger
	          BEFORE UPDATE ON hmily_lock
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()';
END IF;
-- ----------------------------------------------------
-- create table hmily_participant_undo if not exist ---
-- ----------------------------------------------------
IF (SELECT COUNT(1) FROM pg_class  WHERE relname  = 'hmily_participant_undo') > 0 THEN
   RAISE NOTICE 'hmily_participant_undo already exists';
ELSE
    EXECUTE ' CREATE TABLE hmily_participant_undo (
    undo_id INT8  NOT NULL PRIMARY KEY,
    participant_id INT8  NOT NULL ,
    trans_id INT8  NOT NULL ,
    resource_id VARCHAR(256 )  NOT NULL ,
    undo_invocation BYTEA  NOT NULL ,
    status INT2  NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
     )';
	EXECUTE ' COMMENT ON TABLE hmily_participant_undo IS ''' ||'hmily事务参与者undo记录，用在AC模式' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.undo_id IS ''' ||'主键id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.participant_id IS ''' ||'参与者id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.trans_id IS ''' ||'全局事务id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.resource_id IS ''' ||'资源id，at模式下为jdbc url' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.undo_invocation IS ''' ||'回滚调用点' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.status IS ''' ||'状态' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.create_time IS ''' ||'创建时间' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_participant_undo.update_time IS ''' ||'更新时间' || '''';
    -- ------------------------------------------------------
    -- Triggers structure for table hmily_participant_undo---
    -- ------------------------------------------------------
	EXECUTE ' CREATE TRIGGER hmily_participant_undo_tigger
	          BEFORE UPDATE ON hmily_participant_undo
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()';
END IF;
-- ------------------------------------------------------
-- create table hmily_transaction_global if not exist ---
-- ------------------------------------------------------
IF (SELECT COUNT(1) FROM pg_class  WHERE relname  = 'hmily_transaction_global') > 0 THEN
    RAISE NOTICE 'hmily_transaction_global already exists';
ELSE
    EXECUTE 'CREATE TABLE hmily_transaction_global (
    trans_id INT8  NOT NULL PRIMARY KEY,
    app_name VARCHAR(128 )  NOT NULL ,
    status INT2  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    retry INT2  DEFAULT 0  NOT NULL ,
    version INT2  NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
    )';

	EXECUTE ' COMMENT ON TABLE hmily_transaction_global IS ''' ||'hmily事务表（发起者）' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.trans_id IS ''' ||'全局事务id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.app_name IS ''' ||'应用名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.status IS ''' ||'事务状态' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.trans_type IS ''' ||'事务模式' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.retry IS ''' ||'重试次数' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.version IS ''' ||'版本号' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.create_time IS ''' ||'创建时间' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_global.update_time IS ''' ||'更新时间' || '''';
    -- --------------------------------------------------------
    -- Triggers structure for table hmily_transaction_global---
    -- --------------------------------------------------------
	EXECUTE ' CREATE TRIGGER hmily_global_tigger
	          BEFORE UPDATE ON hmily_transaction_global
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()';
END IF;
-- -----------------------------------------------------------
-- create table hmily_transaction_participant if not exist ---
-- -----------------------------------------------------------
IF (SELECT COUNT(1) FROM pg_class  WHERE relname  = 'hmily_transaction_participant') > 0 THEN
    RAISE NOTICE 'hmily_transaction_participant already exists';
ELSE
    EXECUTE 'CREATE TABLE hmily_transaction_participant (
    participant_id INT8  NOT NULL PRIMARY KEY,
    participant_ref_id INT8  ,
    trans_id INT8  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    status INT2  NOT NULL ,
    app_name VARCHAR(64 )  NOT NULL ,
    role INT2  NOT NULL ,
    retry INT2  DEFAULT 0  NOT NULL ,
    target_class VARCHAR(512 )  NOT NULL ,
    target_method VARCHAR(128 )  NOT NULL ,
    confirm_method VARCHAR(128 )  NOT NULL ,
    cancel_method VARCHAR(128 )  NOT NULL ,
    confirm_invocation BYTEA  NOT NULL ,
    cancel_invocation BYTEA  NOT NULL ,
    version INT2 DEFAULT 0 NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
    )';

    EXECUTE ' COMMENT ON TABLE hmily_transaction_participant IS ''' ||'hmily事务参与者' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.participant_id IS ''' ||'参与者事务id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.participant_ref_id IS ''' ||'参与者关联id且套调用时候会存在' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.trans_id IS ''' ||'全局事务id' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.trans_type IS ''' ||'事务类型' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.status IS ''' ||'分支事务状态' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.app_name IS ''' ||'应用名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.role IS ''' ||'事务角色' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.retry IS ''' ||'重试次数' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.target_class IS ''' ||'接口名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.target_method IS ''' ||'接口方法名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.confirm_method IS ''' ||'confirm方法名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.cancel_method IS ''' ||'cancel方法名称' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.confirm_invocation IS ''' ||'confirm调用点' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.cancel_invocation IS ''' ||'cancel调用点' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.create_time IS ''' ||'创建时间' || '''';
	EXECUTE ' COMMENT ON COLUMN hmily_transaction_participant.update_time IS ''' ||'更新时间' || '''';
	-- -------------------------------------------------------------
    -- Triggers structure for table hmily_transaction_participant---
    -- -------------------------------------------------------------
	EXECUTE ' CREATE TRIGGER hmily_participant_tigger
	          BEFORE UPDATE ON hmily_transaction_participant
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()';
END IF;
END
$do$
