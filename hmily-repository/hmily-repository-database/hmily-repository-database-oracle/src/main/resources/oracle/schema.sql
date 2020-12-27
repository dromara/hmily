-- -----------------------------------------
-- init oracle hmily_transaction  tables----
-- -----------------------------------------
DECLARE
     NUM NUMBER;
BEGIN
    -- -----------------------------------------
    -- create table hmily_lock if not exist ----
    -- -----------------------------------------
    SELECT COUNT(1) INTO NUM FROM all_tables WHERE TABLE_NAME = UPPER('hmily_lock') ;
    IF NUM < 1 THEN
      -- -----------------------------------
      -- Table structure for hmily_lock-----
      -- -----------------------------------
      EXECUTE IMMEDIATE 'CREATE TABLE hmily_lock (
            trans_id NUMBER(20)  NOT NULL ,
            participant_id NUMBER(20)  NOT NULL ,
            resource_id VARCHAR2(256 )  NOT NULL ,
            target_table_name VARCHAR2(64 )  NOT NULL ,
            target_table_pk VARCHAR2(64 )  NOT NULL ,
            create_time DATE  NOT NULL ,
            update_time DATE  NOT NULL,
            CONSTRAINT lock_key PRIMARY KEY (resource_id, target_table_name, target_table_pk)
            )';

	  EXECUTE IMMEDIATE ' COMMENT ON TABLE hmily_lock IS ''' ||'hmily全局lock表' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.trans_id  IS ''' ||'全局事务id' || '''';
      EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.participant_id IS ''' ||'hmily参与者id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.resource_id IS ''' ||'资源id'|| '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.target_table_name IS ''' ||'锁定目标表名' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.target_table_pk IS ''' ||'锁定表主键' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.create_time IS ''' ||'创建时间' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_lock.update_time IS ''' ||'更新时间' || '''';
    END IF;


    -- -----------------------------------------------------
    -- create table hmily_participant_undo if not exist ----
    -- -----------------------------------------------------
	SELECT COUNT(1) INTO NUM FROM all_tables WHERE TABLE_NAME = UPPER('hmily_participant_undo') ;
    IF NUM < 1 THEN
      -- -----------------------------------------------
      -- Table structure for hmily_participant_undo-----
       -- -----------------------------------------------
      EXECUTE IMMEDIATE 'CREATE TABLE hmily_participant_undo (
            undo_id NUMBER(20)  NOT NULL PRIMARY KEY,
            participant_id NUMBER(20)  NOT NULL ,
            trans_id NUMBER(20)  NOT NULL ,
            resource_id VARCHAR2(256 )  NOT NULL ,
            data_snapshot BLOB  NOT NULL ,
            status INTEGER  NOT NULL ,
            create_time DATE  NOT NULL ,
            update_time DATE  NOT NULL)';

	  EXECUTE IMMEDIATE ' COMMENT ON TABLE hmily_participant_undo IS ''' ||'hmily事务参与者undo记录，用在AC模式' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.undo_id IS ''' ||'主键id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.participant_id IS ''' ||'参与者id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.trans_id IS ''' ||'全局事务id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.resource_id IS ''' ||'资源id，at模式下为jdbc url' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.data_snapshot IS ''' ||'回滚数据快照' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.status IS ''' ||'状态' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.create_time IS ''' ||'创建时间' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_participant_undo.update_time IS ''' ||'更新时间' || '''';
	END IF;

    -- -------------------------------------------------------
    -- create table hmily_transaction_global if not exist ----
    -- -------------------------------------------------------
	SELECT COUNT(1) INTO NUM FROM all_tables WHERE TABLE_NAME = UPPER('hmily_transaction_global') ;
    IF NUM < 1 THEN
      -- -------------------------------------------------
      -- Table structure for hmily_transaction_global-----
      -- -------------------------------------------------
      EXECUTE IMMEDIATE 'CREATE TABLE hmily_transaction_global (
		    trans_id NUMBER(20)  NOT NULL PRIMARY KEY,
            app_name VARCHAR2(128 )  NOT NULL ,
            status INTEGER  NOT NULL ,
            trans_type VARCHAR2(16 )  NOT NULL ,
            retry INTEGER  DEFAULT 0  NOT NULL ,
            version INTEGER  NOT NULL ,
            create_time DATE  NOT NULL ,
            update_time DATE  NOT NULL)';

	  EXECUTE IMMEDIATE ' COMMENT ON TABLE hmily_transaction_global IS ''' ||'hmily事务表（发起者）' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.trans_id IS ''' ||'全局事务id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.app_name IS ''' ||'应用名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.status IS ''' ||'事务状态' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.trans_type IS ''' ||'事务模式' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.retry IS ''' ||'重试次数' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.version IS ''' ||'版本号' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.create_time IS ''' ||'创建时间' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_global.update_time IS ''' ||'更新时间' || '''';
	END IF;

    -- ------------------------------------------------------------
    -- create table hmily_transaction_participant if not exist ----
    -- ------------------------------------------------------------
	SELECT COUNT(1) INTO NUM FROM all_tables WHERE TABLE_NAME = UPPER('hmily_transaction_participant');
    IF NUM < 1 THEN
      -- -----------------------------------------------------
      -- Table structure for hmily_transaction_participant----
      -- -----------------------------------------------------
      EXECUTE IMMEDIATE 'CREATE TABLE hmily_transaction_participant (
             participant_id NUMBER(20)  NOT NULL PRIMARY KEY,
             participant_ref_id NUMBER(20)  ,
             trans_id NUMBER(20)  NOT NULL ,
             trans_type VARCHAR2(16 )  NOT NULL ,
             status INTEGER  NOT NULL ,
             app_name VARCHAR2(64 )  NOT NULL ,
             role INTEGER  NOT NULL ,
             retry INTEGER  DEFAULT 0  NOT NULL ,
             target_class VARCHAR2(512 )  NULL ,
             target_method VARCHAR2(128 )  NULL ,
             confirm_method VARCHAR2(128 )  NULL ,
             cancel_method VARCHAR2(128 )  NULL ,
             confirm_invocation BLOB  NULL ,
             cancel_invocation BLOB  NULL ,
             version INTEGER DEFAULT 0 NOT NULL ,
             create_time DATE  NOT NULL ,
             update_time DATE  NOT NULL)';

	  EXECUTE IMMEDIATE ' COMMENT ON TABLE hmily_transaction_participant IS ''' ||'hmily事务参与者' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.participant_id IS ''' ||'参与者事务id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.participant_ref_id IS ''' ||'参与者关联id且套调用时候会存在' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.trans_id IS ''' ||'全局事务id' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.trans_type IS ''' ||'事务类型' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.status IS ''' ||'分支事务状态' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.app_name IS ''' ||'应用名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.role IS ''' ||'事务角色' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.retry IS ''' ||'重试次数' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.target_class IS ''' ||'接口名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.target_method IS ''' ||'接口方法名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.confirm_method IS ''' ||'confirm方法名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.cancel_method IS ''' ||'cancel方法名称' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.confirm_invocation IS ''' ||'confirm调用点' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.cancel_invocation IS ''' ||'cancel调用点' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.create_time IS ''' ||'创建时间' || '''';
	  EXECUTE IMMEDIATE ' COMMENT ON COLUMN hmily_transaction_participant.update_time IS ''' ||'更新时间' || '''';

	END IF;
END;

/
-- ---------------------------------------------
-- Triggers structure for table hmily_lock------
-- ---------------------------------------------
CREATE TRIGGER hmily_lock_tigger BEFORE UPDATE OF update_time ON hmily_lock REFERENCING  OLD AS OLD NEW AS NEW FOR  EACH ROW
BEGIN
	:new.update_time := sysdate;
END;
/
-- ------------------------------------------------------
-- Triggers structure for table hmily_participant_undo---
-- ------------------------------------------------------
CREATE TRIGGER hmily_participant_undo_tigger BEFORE UPDATE OF update_time ON hmily_participant_undo REFERENCING  OLD AS OLD NEW AS NEW FOR  EACH ROW
BEGIN
	:new.update_time := sysdate;
END;
/
-- --------------------------------------------------------
-- Triggers structure for table hmily_transaction_global---
-- --------------------------------------------------------
CREATE TRIGGER hmily_global_tigger BEFORE UPDATE OF update_time ON hmily_transaction_global REFERENCING  OLD AS OLD NEW AS NEW FOR  EACH ROW
BEGIN
	:new.update_time := sysdate;
END;
/
-- -------------------------------------------------------------
-- Triggers structure for table hmily_transaction_participant---
-- -------------------------------------------------------------
CREATE TRIGGER hmily_participant_tigger BEFORE UPDATE OF update_time ON hmily_transaction_participant REFERENCING  OLD AS OLD NEW AS NEW FOR  EACH ROW
BEGIN
	:new.update_time := sysdate;
END;
/