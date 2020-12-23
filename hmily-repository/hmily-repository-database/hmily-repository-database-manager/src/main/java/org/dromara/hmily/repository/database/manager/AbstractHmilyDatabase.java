/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.repository.database.manager;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.config.api.ConfigEnv;
import org.dromara.hmily.config.api.entity.HmilyConfig;
import org.dromara.hmily.config.api.entity.HmilyDatabaseConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyLock;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyParticipantUndo;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Abstract hmily database.
 */
@Slf4j
public abstract class AbstractHmilyDatabase implements HmilyRepository {
    
    /**
     * The constant INSERT_HMILY_TRANSACTION.
     */
    protected static final String INSERT_HMILY_TRANSACTION = "INSERT INTO hmily_transaction_global (trans_id, app_name, status, trans_type, "
            + "retry, version, create_time, update_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    
    /**
     * The constant SELECT_HMILY_TRANSACTION_COMMON.
     */
    protected static final String SELECT_HMILY_TRANSACTION_COMMON = "select trans_id, app_name, status, trans_type, retry, version from hmily_transaction_global ";
    
    /**
     * The constant SELECT_HMILY_TRANSACTION_DELAY.
     */
    protected static final String SELECT_HMILY_TRANSACTION_DELAY = SELECT_HMILY_TRANSACTION_COMMON + " where update_time < ? and app_name = ?";
    
    /**
     * The constant SELECT_HMILY_TRANSACTION_WITH_TRANS_ID.
     */
    protected static final String SELECT_HMILY_TRANSACTION_WITH_TRANS_ID = SELECT_HMILY_TRANSACTION_COMMON + " where trans_id = ?";
    
    /**
     * The constant UPDATE_HMILY_TRANSACTION_STATUS.
     */
    protected static final String UPDATE_HMILY_TRANSACTION_STATUS = "update hmily_transaction_global  set status=?  where trans_id = ? ";
    
    /**
     * The constant UPDATE_HMILY_TRANSACTION_RETRY_LOCK.
     */
    protected static final String UPDATE_HMILY_TRANSACTION_RETRY_LOCK = "update hmily_transaction_global set version =?, retry =? where trans_id = ? and version = ? ";
    
    /**
     * The constant UPDATE_HMILY_PARTICIPANT_LOCK.
     */
    protected static final String UPDATE_HMILY_PARTICIPANT_LOCK = "update hmily_transaction_participant set version =?, retry =? where participant_id = ? and version = ? ";
    
    /**
     * The constant DELETE_HMILY_TRANSACTION.
     */
    protected static final String DELETE_HMILY_TRANSACTION = "delete from hmily_transaction_global where trans_id = ? ";
    
    /**
     * The constant DELETE_HMILY_TRANSACTION_WITH_DATA.
     */
    protected static final String DELETE_HMILY_TRANSACTION_WITH_DATA = "delete from hmily_transaction_global where update_time < ? and status = 4";
    
    /**
     * The constant INSERT_HMILY_PARTICIPANT.
     */
    protected static final String INSERT_HMILY_PARTICIPANT = "INSERT INTO hmily_transaction_participant (participant_id, participant_ref_id, trans_id, trans_type, status, app_name,"
            + "role, retry, target_class, target_method, confirm_method, cancel_method, confirm_invocation, cancel_invocation, version, create_time, update_time)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? , ? , ?, ?)";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_COMMON.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_COMMON = "select participant_id, participant_ref_id, trans_id, trans_type, status, app_name,"
            + "role, retry, target_class, target_method, confirm_method, cancel_method, confirm_invocation, cancel_invocation, version from hmily_transaction_participant ";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_WITH_KEY.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_KEY = SELECTOR_HMILY_PARTICIPANT_COMMON + " where participant_id = ?";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_WITH_PARTICIPANT_REF_ID.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_PARTICIPANT_REF_ID = SELECTOR_HMILY_PARTICIPANT_COMMON + " where participant_ref_id = ?";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_WITH_TRANS_ID.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_TRANS_ID = SELECTOR_HMILY_PARTICIPANT_COMMON + " where trans_id = ?";
    
    /**
     * The constant EXIST_HMILY_PARTICIPANT_WITH_TRANS_ID.
     */
    protected static final String EXIST_HMILY_PARTICIPANT_WITH_TRANS_ID = " select count(*) as count_total from hmily_transaction_participant where trans_id = ? ";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME_TRANS_TYPE.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME_TRANS_TYPE = SELECTOR_HMILY_PARTICIPANT_COMMON
            + " where update_time < ? and app_name = ?  and trans_type = ? and status not in (4, 8) ";
    
    /**
     * The constant UPDATE_HMILY_PARTICIPANT_STATUS.
     */
    protected static final String UPDATE_HMILY_PARTICIPANT_STATUS = "update hmily_transaction_participant set status=? where participant_id = ? ";
    
    /**
     * The constant DELETE_HMILY_PARTICIPANT.
     */
    protected static final String DELETE_HMILY_PARTICIPANT = "delete from hmily_transaction_participant where participant_id = ? ";
    
    /**
     * The constant DELETE_HMILY_PARTICIPANT_WITH_DATA.
     */
    protected static final String DELETE_HMILY_PARTICIPANT_WITH_DATA = "delete from hmily_transaction_participant where update_time < ? and status = 4";
    
    /**
     * The constant INSERT_HMILY_PARTICIPANT_UNDO.
     */
    protected static final String INSERT_HMILY_PARTICIPANT_UNDO = "INSERT INTO hmily_participant_undo"
            + "(undo_id, participant_id, trans_id, resource_id, data_snapshot, status, create_time, update_time) "
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    /**
     * The constant SELECTOR_HMILY_PARTICIPANT_UNDO_WITH_PARTICIPANT_ID.
     */
    protected static final String SELECTOR_HMILY_PARTICIPANT_UNDO_WITH_PARTICIPANT_ID = " select undo_id, participant_id, trans_id, resource_id, data_snapshot, status "
            + "from hmily_participant_undo where participant_id =? ";
    
    /**
     * The constant REMOVE_HMILY_PARTICIPANT_UNDO.
     */
    protected static final String DELETE_HMILY_PARTICIPANT_UNDO = "delete from hmily_participant_undo where undo_id = ?";
    
    /**
     * The constant DELETE_HMILY_PARTICIPANT_UNDO_WITH_DATA.
     */
    protected static final String DELETE_HMILY_PARTICIPANT_UNDO_WITH_DATA = "delete from hmily_participant_undo where update_time < ? and status = 4";
    
    /**
     * The constant UPDATE_HMILY_PARTICIPANT_UNDO_STATUS.
     */
    protected static final String UPDATE_HMILY_PARTICIPANT_UNDO_STATUS = "update hmily_participant_undo set status=? where undo_id = ? ";
    
    /**
     * The constant INSERT_HMILY_LOCK.
     */
    protected static final String INSERT_HMILY_LOCK = "INSERT INTO hmily_lock"
        + "(trans_id, participant_id, resource_id, target_table_name, target_table_pk, create_time, update_time) "
        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    /**
     * The constant DELETE_HMILY_LOCK.
     */
    protected static final String DELETE_HMILY_LOCK = "delete from hmily_lock where resource_id = ? and target_table_name = ? and target_table_pk = ?";
    
    /**
     * The constant SELECT_HMILY_LOCK_BY_PK.
     */
    protected static final String SELECT_HMILY_LOCK_BY_PK = " select trans_id, participant_id, resource_id, target_table_name, target_table_pk from hmily_lock where "
        + "resource_id = ? and target_table_name = ? and target_table_pk = ?";
    
    /**
     * The data source.
     */
    private DataSource dataSource;
    
    /**
     * The hmily serializer.
     */
    private HmilySerializer hmilySerializer;
    
    /**
     * The App name.
     */
    private String appName;
    
    /**
     * Hmily transaction limit sql string.
     *
     * @param limit the limit
     * @return the string
     */
    protected abstract String hmilyTransactionLimitSql(int limit);
    
    /**
     * Hmily participant limit sql string.
     *
     * @param limit the limit
     * @return the string
     */
    protected abstract String hmilyParticipantLimitSql(int limit);
    
    /**
     * Execte schema.sql by different database.
     *
     * @param hmilyDbConfig the hmilyDbConfig
     * @throws Exception the exception
     */
    protected abstract void initScript(HmilyDatabaseConfig hmilyDbConfig) throws Exception;
    
    /**
     * Convert data type object.
     *
     * @param params the params
     * @return the object
     */
    protected abstract Object convertDataType(Object params);
    
    @Override
    public void init(final String appName) {
        this.appName = appName;
        try {
            HmilyDatabaseConfig hmilyDatabaseConfig = ConfigEnv.getInstance().getConfig(HmilyDatabaseConfig.class);
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(hmilyDatabaseConfig.getUrl());
            hikariDataSource.setDriverClassName(hmilyDatabaseConfig.getDriverClassName());
            hikariDataSource.setUsername(hmilyDatabaseConfig.getUsername());
            hikariDataSource.setPassword(hmilyDatabaseConfig.getPassword());
            hikariDataSource.setMaximumPoolSize(hmilyDatabaseConfig.getMaxActive());
            hikariDataSource.setMinimumIdle(hmilyDatabaseConfig.getMinIdle());
            hikariDataSource.setConnectionTimeout(hmilyDatabaseConfig.getConnectionTimeout());
            hikariDataSource.setIdleTimeout(hmilyDatabaseConfig.getIdleTimeout());
            hikariDataSource.setMaxLifetime(hmilyDatabaseConfig.getMaxLifetime());
            hikariDataSource.setConnectionTestQuery(hmilyDatabaseConfig.getConnectionTestQuery());
            if (hmilyDatabaseConfig.getPropertyMap() != null && !hmilyDatabaseConfig.getPropertyMap().isEmpty()) {
                hmilyDatabaseConfig.getPropertyMap().forEach(hikariDataSource::addDataSourceProperty);
            }
            HmilyConfig hmilyConfig = ConfigEnv.getInstance().getConfig(HmilyConfig.class);
            this.dataSource = hikariDataSource;
            if (hmilyConfig.isAutoSql()) {
                this.initScript(hmilyDatabaseConfig);
            }
        } catch (Exception e) {
            log.error("hmily jdbc log init exception please check config:{}", e.getMessage());
            throw new HmilyRuntimeException(e.getMessage());
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public HmilyTransaction findByTransId(final Long transId) {
        List<Map<String, Object>> list = executeQuery(SELECT_HMILY_TRANSACTION_WITH_TRANS_ID, transId);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildHmilyTransactionByResultMap)
                    .findFirst().orElse(null);
        }
        return null;
    }
    
    @Override
    public int createHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        return executeUpdate(INSERT_HMILY_TRANSACTION, hmilyTransaction.getTransId(), appName, hmilyTransaction.getStatus(),
                hmilyTransaction.getTransType(), hmilyTransaction.getRetry(), hmilyTransaction.getVersion(), hmilyTransaction.getCreateTime(), hmilyTransaction.getUpdateTime());
    }
    
    @Override
    public int updateRetryByLock(final HmilyTransaction hmilyTransaction) {
        final Integer currentVersion = hmilyTransaction.getVersion();
        hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
        hmilyTransaction.setRetry(hmilyTransaction.getRetry() + 1);
        return executeUpdate(UPDATE_HMILY_TRANSACTION_RETRY_LOCK, hmilyTransaction.getVersion(), hmilyTransaction.getRetry(), hmilyTransaction.getTransId(), currentVersion);
    }
    
    @Override
    public boolean lockHmilyParticipant(final HmilyParticipant hmilyParticipant) {
        Integer currentVersion = hmilyParticipant.getVersion();
        hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
        hmilyParticipant.setRetry(hmilyParticipant.getRetry() + 1);
        return executeUpdate(UPDATE_HMILY_PARTICIPANT_LOCK, hmilyParticipant.getVersion(), hmilyParticipant.getRetry(), hmilyParticipant.getParticipantId(), currentVersion) > 0;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(final Long transId) {
        List<Map<String, Object>> participantList = executeQuery(EXIST_HMILY_PARTICIPANT_WITH_TRANS_ID, transId);
        if (CollectionUtils.isNotEmpty(participantList)) {
            Long total = participantList.stream()
                    .filter(Objects::nonNull)
                    .map(e -> (Long) e.get("count_total")).findFirst().orElse(0L);
            return total > 0;
        }
        return false;
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipantByTransId(final Long transId) {
        List<Map<String, Object>> participantList = executeQuery(SELECTOR_HMILY_PARTICIPANT_WITH_TRANS_ID, transId);
        if (CollectionUtils.isNotEmpty(participantList)) {
            return participantList.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildHmilyParticipantByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<HmilyParticipant> listHmilyParticipant(final Date date, final String transType, final int limit) {
        String limitSql = hmilyParticipantLimitSql(limit);
        List<Map<String, Object>> participantList = executeQuery(limitSql, date, appName, transType);
        if (CollectionUtils.isNotEmpty(participantList)) {
            return participantList.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildHmilyParticipantByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<HmilyTransaction> listLimitByDelay(final Date date, final int limit) {
        String limitSql = hmilyTransactionLimitSql(limit);
        List<Map<String, Object>> list = executeQuery(limitSql, date, appName);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildHmilyTransactionByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    @Override
    public int updateHmilyTransactionStatus(final Long transId, final Integer status) throws HmilyRepositoryException {
        return executeUpdate(UPDATE_HMILY_TRANSACTION_STATUS, status, transId);
    }
    
    @Override
    public int removeHmilyTransaction(final Long transId) {
        return executeUpdate(DELETE_HMILY_TRANSACTION, transId);
    }
    
    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        byte[] confirmSerialize = null;
        byte[] cancelSerialize = null;
        if (Objects.nonNull(hmilyParticipant.getConfirmHmilyInvocation())) {
            confirmSerialize = hmilySerializer.serialize(hmilyParticipant.getConfirmHmilyInvocation());
        }
        if (Objects.nonNull(hmilyParticipant.getCancelHmilyInvocation())) {
            cancelSerialize = hmilySerializer.serialize(hmilyParticipant.getCancelHmilyInvocation());
        }
        return executeUpdate(INSERT_HMILY_PARTICIPANT, hmilyParticipant.getParticipantId(), hmilyParticipant.getParticipantRefId(),
                hmilyParticipant.getTransId(), hmilyParticipant.getTransType(), hmilyParticipant.getStatus(),
                appName, hmilyParticipant.getRole(), hmilyParticipant.getRetry(), hmilyParticipant.getTargetClass(), hmilyParticipant.getTargetMethod(),
                hmilyParticipant.getConfirmMethod(), hmilyParticipant.getCancelMethod(), confirmSerialize, cancelSerialize,
                hmilyParticipant.getVersion(), hmilyParticipant.getCreateTime(), hmilyParticipant.getUpdateTime());
    }
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(final Long participantId) {
        List<HmilyParticipant> hmilyParticipantList = new ArrayList<>();
        List<Map<String, Object>> result = executeQuery(SELECTOR_HMILY_PARTICIPANT_WITH_KEY, participantId);
        if (CollectionUtils.isNotEmpty(result)) {
            HmilyParticipant hmilyParticipant = result.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildHmilyParticipantByResultMap)
                    .findFirst().orElse(new HmilyParticipant());
            hmilyParticipantList.add(hmilyParticipant);
            //get ref
            List<Map<String, Object>> refParticipants = executeQuery(SELECTOR_HMILY_PARTICIPANT_WITH_PARTICIPANT_REF_ID, participantId);
            if (CollectionUtils.isNotEmpty(refParticipants)) {
                List<HmilyParticipant> refParticipantList = refParticipants.stream()
                        .filter(Objects::nonNull)
                        .map(this::buildHmilyParticipantByResultMap)
                        .collect(Collectors.toList());
                hmilyParticipantList.addAll(refParticipantList);
            }
        }
        return hmilyParticipantList;
    }
    
    @Override
    public int createHmilyParticipantUndo(final HmilyParticipantUndo undo) {
        byte[] dataSnapshot = hmilySerializer.serialize(undo.getDataSnapshot());
        return executeUpdate(INSERT_HMILY_PARTICIPANT_UNDO, undo.getUndoId(), undo.getParticipantId(), undo.getTransId(), undo.getResourceId(),
                dataSnapshot, undo.getStatus(), undo.getCreateTime(), undo.getUpdateTime());
    }
    
    @Override
    public List<HmilyParticipantUndo> findHmilyParticipantUndoByParticipantId(final Long participantId) {
        List<Map<String, Object>> results = executeQuery(SELECTOR_HMILY_PARTICIPANT_UNDO_WITH_PARTICIPANT_ID, participantId);
        if (CollectionUtils.isEmpty(results)) {
            return Collections.emptyList();
        }
        return results.stream().map(this::buildHmilyParticipantUndoByResultMap).collect(Collectors.toList());
    }
    
    @Override
    public int updateHmilyParticipantUndoStatus(final Long undoId, final Integer status) {
        return executeUpdate(UPDATE_HMILY_PARTICIPANT_UNDO_STATUS, status, undoId);
    }
    
    @Override
    public int removeHmilyTransactionByDate(final Date date) {
        return executeUpdate(DELETE_HMILY_TRANSACTION_WITH_DATA, date);
    }
    
    @Override
    public int removeHmilyParticipantByDate(final Date date) {
        return executeUpdate(DELETE_HMILY_PARTICIPANT_WITH_DATA, date);
    }
    
    @Override
    public int removeHmilyParticipantUndoByDate(final Date date) {
        return executeUpdate(DELETE_HMILY_PARTICIPANT_UNDO_WITH_DATA, date);
    }
    
    @Override
    public int removeHmilyParticipantUndo(final Long undoId) {
        return executeUpdate(DELETE_HMILY_PARTICIPANT_UNDO, undoId);
    }
    
    @Override
    public int updateHmilyParticipantStatus(final Long participantId, final Integer status) {
        return executeUpdate(UPDATE_HMILY_PARTICIPANT_STATUS, status, participantId);
    }
    
    @Override
    public int removeHmilyParticipant(final Long participantId) {
        return executeUpdate(DELETE_HMILY_PARTICIPANT, participantId);
    }
    
    @Override
    public int writeHmilyLocks(final Collection<HmilyLock> locks) {
        List<List<Object>> params = new LinkedList<>();
        for (HmilyLock each : locks) {
            List<Object> group = new LinkedList<>();
            group.add(each.getTransId());
            group.add(each.getParticipantId());
            group.add(each.getResourceId());
            group.add(each.getTargetTableName());
            group.add(each.getTargetTablePk());
            group.add(new Date());
            group.add(new Date());
            params.add(group);
        }
        return batchExecuteUpdate(INSERT_HMILY_LOCK, params);
    }
    
    @Override
    public int releaseHmilyLocks(final Collection<HmilyLock> locks) {
        List<List<Object>> params = new LinkedList<>();
        for (HmilyLock each : locks) {
            List<Object> group = new LinkedList<>();
            group.add(each.getResourceId());
            group.add(each.getTargetTableName());
            group.add(each.getTargetTablePk());
            params.add(group);
        }
        return batchExecuteUpdate(DELETE_HMILY_LOCK, params);
    }
    
    @Override
    public Optional<HmilyLock> findHmilyLockById(final String lockId) {
        List<Map<String, Object>> list = executeQuery(SELECT_HMILY_LOCK_BY_PK, Splitter.on(";;").splitToList(lockId).toArray());
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull).map(this::buildHmilyLockByResultMap).findFirst();
        }
        return Optional.empty();
    }
    
    private int batchExecuteUpdate(final String sql, final List<List<Object>> params) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (List<Object> each : params) {
                    int index = 1;
                    for (Object param : each) {
                        ps.setObject(index, param);
                        index++;
                    }
                    ps.addBatch();
                }
                int[] count = ps.executeBatch();
                if (params.size() != Arrays.stream(count).sum()) {
                    con.rollback();
                    return 0;
                }
                con.commit();
                return params.size();
            } catch (SQLException ex) {
                con.rollback();
                log.error("hmily jdbc batchExecuteUpdate repository exception -> ", ex);
                return FAIL_ROWS;
            }
        } catch (SQLException ex) {
            return FAIL_ROWS;
        }
    }
    
    /**
     * Execute update int.
     *
     * @param sql    the sql
     * @param params the params
     * @return the int
     */
    private int executeUpdate(final String sql, final Object... params) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = createPreparedStatement(con, sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("hmily jdbc executeUpdate repository exception -> ", e);
            return FAIL_ROWS;
        }
    }
    
    private List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        List<Map<String, Object>> list = null;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = createPreparedStatement(con, sql, params);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = Maps.newHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i).toLowerCase(), convertDataType(rs.getObject(i)));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("hmily jdbc executeQuery repository exception -> ", e);
        }
        return list;
    }
    
    private PreparedStatement createPreparedStatement(final Connection con, final String sql, final Object... params) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, convertDataType(params[i]));
            }
        }
        return ps;
    }
    
    private HmilyTransaction buildHmilyTransactionByResultMap(final Map<String, Object> map) {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId((Long) map.get("trans_id"));
        hmilyTransaction.setTransType((String) map.get("trans_type"));
        hmilyTransaction.setStatus(Integer.parseInt(map.get("status").toString()));
        hmilyTransaction.setAppName((String) map.get("app_name"));
        hmilyTransaction.setRetry(Integer.parseInt(map.get("retry").toString()));
        hmilyTransaction.setVersion(Integer.parseInt((map.get("version")).toString()));
        return hmilyTransaction;
    }
    
    private HmilyParticipantUndo buildHmilyParticipantUndoByResultMap(final Map<String, Object> map) {
        HmilyParticipantUndo undo = new HmilyParticipantUndo();
        undo.setUndoId((Long) map.get("undo_id"));
        undo.setParticipantId((Long) map.get("participant_id"));
        undo.setTransId((Long) map.get("trans_id"));
        undo.setResourceId((String) map.get("resource_id"));
        byte[] snapshotBytes = (byte[]) map.get("data_snapshot");
        try {
            HmilyDataSnapshot snapshot = hmilySerializer.deSerialize(snapshotBytes, HmilyDataSnapshot.class);
            undo.setDataSnapshot(snapshot);
        } catch (HmilySerializerException e) {
            log.error("hmilySerializer deSerialize have exception:{} ", e.getMessage());
        }
        undo.setStatus(Integer.parseInt((map.get("status")).toString()));
        return undo;
    }
    
    private HmilyParticipant buildHmilyParticipantByResultMap(final Map<String, Object> map) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId((Long) map.get("participant_id"));
        hmilyParticipant.setParticipantRefId((Long) map.get("participant_ref_id"));
        hmilyParticipant.setTransId((Long) map.get("trans_id"));
        hmilyParticipant.setTransType((String) map.get("trans_type"));
        hmilyParticipant.setStatus(Integer.parseInt((map.get("status")).toString()));
        hmilyParticipant.setRole(Integer.parseInt((map.get("role")).toString()));
        hmilyParticipant.setRetry(Integer.parseInt((map.get("retry")).toString()));
        hmilyParticipant.setAppName((String) map.get("app_name"));
        hmilyParticipant.setTargetClass((String) map.get("target_class"));
        hmilyParticipant.setTargetMethod((String) map.get("target_method"));
        hmilyParticipant.setConfirmMethod((String) map.get("confirm_method"));
        hmilyParticipant.setCancelMethod((String) map.get("cancel_method"));
        try {
            if (Objects.nonNull(map.get("confirm_invocation"))) {
                byte[] confirmInvocation = (byte[]) map.get("confirm_invocation");
                final HmilyInvocation confirmHmilyInvocation = hmilySerializer.deSerialize(confirmInvocation, HmilyInvocation.class);
                hmilyParticipant.setConfirmHmilyInvocation(confirmHmilyInvocation);
            }
            if (Objects.nonNull(map.get("cancel_invocation"))) {
                byte[] cancelInvocation = (byte[]) map.get("cancel_invocation");
                final HmilyInvocation cancelHmilyInvocation = hmilySerializer.deSerialize(cancelInvocation, HmilyInvocation.class);
                hmilyParticipant.setCancelHmilyInvocation(cancelHmilyInvocation);
            }
        } catch (HmilySerializerException e) {
            log.error("hmilySerializer deSerialize have exception:{} ", e.getMessage());
        }
        hmilyParticipant.setVersion(Integer.parseInt((map.get("version")).toString()));
        return hmilyParticipant;
    }
    
    private HmilyLock buildHmilyLockByResultMap(final Map<String, Object> map) {
        Long transId = (Long) map.get("trans_id");
        Long participantId = (Long) map.get("participant_id");
        String resourceId = (String) map.get("resource_id");
        String targetTableName = (String) map.get("target_table_name");
        String targetTablePk = (String) map.get("target_table_pk");
        return new HmilyLock(transId, participantId, resourceId, targetTableName, targetTablePk);
    }
}
