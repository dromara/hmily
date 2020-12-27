CREATE EXTENSION IF NOT EXISTS dblink;
DO
$do$
DECLARE
  _db TEXT := 'hmily';
  _user TEXT := 'userName';
  _password TEXT := 'password';
	_tablelock INTEGER :=0;
BEGIN
  IF EXISTS (SELECT 1 FROM pg_database WHERE datname = _db) THEN
    RAISE NOTICE 'Database already exists';
  ELSE
    PERFORM public.dblink_connect('host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||current_database());
    PERFORM public.dblink_exec('CREATE DATABASE ' || _db );
  END IF;

	PERFORM public.dblink_connect('init_conn','host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||_db);
	PERFORM public.dblink_exec('init_conn', 'BEGIN');
    PERFORM public.dblink_exec('init_conn','CREATE OR REPLACE FUNCTION update_timestamp() RETURNS TRIGGER AS
                                          $$
                                          BEGIN
                                          NEW.update_time = current_timestamp;
                                          RETURN NEW;
                                          END
                                          $$
                                          language plpgsql;');
	PERFORM public.dblink_exec('init_conn', 'COMMIT');
-- ----------------------------------------
-- create table hmily_lock if not exist ---
-- ----------------------------------------
IF (SELECT * FROM dblink('host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||_db,'SELECT COUNT(1) FROM pg_class  WHERE relname  = ''' ||'hmily' || '''')AS t(count BIGINT) )> 0 THEN
    RAISE NOTICE 'hmily_lock already exists';
ELSE
    PERFORM public.dblink_exec('init_conn', 'BEGIN');
		PERFORM public.dblink_exec('init_conn', 'CREATE TABLE  hmily_lock (
    trans_id INT8 NOT NULL,
    participant_id INT8 NOT NULL,
    resource_id VARCHAR(256) NOT NULL,
    target_table_name VARCHAR(64) NOT NULL,
    target_table_pk VARCHAR(64) NOT NULL,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp,
    PRIMARY KEY (resource_id, target_table_name, target_table_pk)
    )');

	PERFORM public.dblink_exec('init_conn','COMMENT ON TABLE hmily_lock IS ''' ||'hmily全局lock表' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.trans_id  IS ''' ||'全局事务id' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.participant_id IS ''' ||'hmily参与者id' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.resource_id IS ''' ||'资源id'|| '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.target_table_name IS ''' ||'锁定目标表名' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.target_table_pk IS ''' ||'锁定表主键' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.create_time IS ''' ||'创建时间' || '''');
	PERFORM public.dblink_exec('init_conn','COMMENT ON COLUMN hmily_lock.update_time IS ''' ||'更新时间' || '''');
	PERFORM public.dblink_exec('init_conn','CREATE TRIGGER hmily_lock_trigger
	                              BEFORE UPDATE ON hmily_lock
	                              FOR EACH ROW EXECUTE PROCEDURE update_timestamp()');
	PERFORM public.dblink_exec('init_conn', 'COMMIT');
END IF;
-- ----------------------------------------------------
-- create table hmily_participant_undo if not exist ---
-- ----------------------------------------------------
IF (SELECT * FROM dblink('host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||_db,'SELECT COUNT(1) FROM pg_class  WHERE relname  = ''' ||'hmily_participant_undo' || '''')AS t(count BIGINT) )> 0 THEN
    RAISE NOTICE 'hmily_participant_undo already exists';
ELSE
    PERFORM public.dblink_exec('init_conn', 'BEGIN');
    PERFORM public.dblink_exec('init_conn', ' CREATE TABLE hmily_participant_undo (
    undo_id INT8  NOT NULL PRIMARY KEY,
    participant_id INT8  NOT NULL ,
    trans_id INT8  NOT NULL ,
    resource_id VARCHAR(256 )  NOT NULL ,
    data_snapshot BYTEA  NOT NULL ,
    status INT2  NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
     )');
	PERFORM public.dblink_exec('init_conn', ' COMMENT ON TABLE hmily_participant_undo IS ''' ||'hmily事务参与者undo记录，用在AC模式' || '''');
	PERFORM public.dblink_exec('init_conn', ' COMMENT ON COLUMN hmily_participant_undo.undo_id IS ''' ||'主键id' || '''');
	PERFORM public.dblink_exec('init_conn', ' COMMENT ON COLUMN hmily_participant_undo.participant_id IS ''' ||'参与者id' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.trans_id IS ''' ||'全局事务id' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.resource_id IS ''' ||'资源id，at模式下为jdbc url' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.data_snapshot IS ''' ||'回滚数据快照' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.status IS ''' ||'状态' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.create_time IS ''' ||'创建时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_participant_undo.update_time IS ''' ||'更新时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' CREATE TRIGGER hmily_participant_undo_tigger
	          BEFORE UPDATE ON hmily_participant_undo
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()');
	PERFORM public.dblink_exec('init_conn', 'COMMIT');
END IF;
-- ------------------------------------------------------
-- create table hmily_transaction_global if not exist ---
-- ------------------------------------------------------
IF (SELECT * FROM dblink('host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||_db,'SELECT COUNT(1) FROM pg_class  WHERE relname  = ''' ||'hmily_transaction_global' || '''')AS t(count BIGINT) )> 0 THEN
    RAISE NOTICE 'hmily_transaction_global already exists';
ELSE
    PERFORM public.dblink_exec('init_conn', 'BEGIN');
    PERFORM public.dblink_exec('init_conn',  'CREATE TABLE hmily_transaction_global (
    trans_id INT8  NOT NULL PRIMARY KEY,
    app_name VARCHAR(128 )  NOT NULL ,
    status INT2  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    retry INT2  DEFAULT 0  NOT NULL ,
    version INT2  NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
    )');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON TABLE hmily_transaction_global IS ''' ||'hmily事务表（发起者）' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.trans_id IS ''' ||'全局事务id' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.app_name IS ''' ||'应用名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.status IS ''' ||'事务状态' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.trans_type IS ''' ||'事务模式' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.retry IS ''' ||'重试次数' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.version IS ''' ||'版本号' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.create_time IS ''' ||'创建时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_global.update_time IS ''' ||'更新时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' CREATE TRIGGER hmily_global_tigger
	          BEFORE UPDATE ON hmily_transaction_global
	          FOR EACH ROW EXECUTE PROCEDURE update_timestamp()');
	PERFORM public.dblink_exec('init_conn', 'COMMIT');
END IF;
-- -----------------------------------------------------------
-- create table hmily_transaction_participant if not exist ---
-- -----------------------------------------------------------
IF (SELECT * FROM dblink('host=localhost user=' || _user || ' password=' || _password || ' dbname=' ||_db,'SELECT COUNT(1) FROM pg_class  WHERE relname  = ''' ||'hmily_transaction_participant' || '''')AS t(count BIGINT) )> 0 THEN
    RAISE NOTICE 'hmily_transaction_participant already exists';
ELSE
    PERFORM public.dblink_exec('init_conn', 'BEGIN');
    PERFORM public.dblink_exec('init_conn',  'CREATE TABLE hmily_transaction_participant (
    participant_id INT8  NOT NULL PRIMARY KEY,
    participant_ref_id INT8  ,
    trans_id INT8  NOT NULL ,
    trans_type VARCHAR(16 )  NOT NULL ,
    status INT2  NOT NULL ,
    app_name VARCHAR(64 )  NOT NULL ,
    role INT2  NOT NULL ,
    retry INT2  DEFAULT 0  NOT NULL ,
    target_class VARCHAR(512 )  NULL ,
    target_method VARCHAR(128 )  NULL ,
    confirm_method VARCHAR(128 )  NULL ,
    cancel_method VARCHAR(128 )  NULL ,
    confirm_invocation BYTEA  NULL ,
    cancel_invocation BYTEA  NULL ,
    version INT2 DEFAULT 0 NOT NULL ,
    create_time TIMESTAMP(6) NOT NULL default current_timestamp,
    update_time TIMESTAMP(6) NOT NULL default current_timestamp
    )');

    PERFORM public.dblink_exec('init_conn',  ' COMMENT ON TABLE hmily_transaction_participant IS ''' ||'hmily事务参与者' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.participant_id IS ''' ||'参与者事务id' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.participant_ref_id IS ''' ||'参与者关联id且套调用时候会存在' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.trans_id IS ''' ||'全局事务id' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.trans_type IS ''' ||'事务类型' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.status IS ''' ||'分支事务状态' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.app_name IS ''' ||'应用名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.role IS ''' ||'事务角色' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.retry IS ''' ||'重试次数' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.target_class IS ''' ||'接口名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.target_method IS ''' ||'接口方法名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.confirm_method IS ''' ||'confirm方法名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.cancel_method IS ''' ||'cancel方法名称' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.confirm_invocation IS ''' ||'confirm调用点' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.cancel_invocation IS ''' ||'cancel调用点' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.create_time IS ''' ||'创建时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' COMMENT ON COLUMN hmily_transaction_participant.update_time IS ''' ||'更新时间' || '''');
	PERFORM public.dblink_exec('init_conn',  ' CREATE TRIGGER hmily_participant_tigger
	                                           BEFORE UPDATE ON hmily_transaction_participant
	                                           FOR EACH ROW EXECUTE PROCEDURE update_timestamp()');
	PERFORM public.dblink_exec('init_conn', 'COMMIT');
END IF;
    PERFORM public.dblink_disconnect('init_conn');
END
$do$;