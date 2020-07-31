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

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyDbConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyInvocation;
import org.dromara.hmily.repository.spi.entity.HmilyParticipant;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;
import org.dromara.hmily.serializer.spi.exception.HmilySerializerException;

/**
 * The type Abstract hmily database.
 */
@Slf4j
public abstract class AbstractHmilyDatabase implements HmilyRepository {
    
    protected static final String INSERT_HMILY_TRANSACTION = "INSERT INTO hmily_transaction_global (trans_id, app_name, status, trans_type, "
            + "retry, version, create_time, update_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    
    protected static final String SELECT_HMILY_TRANSACTION_COMMON = "select trans_id, app_name, status, trans_type, retry, version from hmily_transaction_global ";
    
    protected static final String SELECT_HMILY_TRANSACTION_DELAY = SELECT_HMILY_TRANSACTION_COMMON + " where update_time < ? and app_name = ?";
    
    protected static final String SELECT_HMILY_TRANSACTION_WITH_TRANS_ID = SELECT_HMILY_TRANSACTION_COMMON + " where trans_id = ?";
    
    protected static final String UPDATE_HMILY_TRANSACTION_STATUS = "update hmily_transaction_global  set status=?  where trans_id = ? ";
    
    protected static final String UPDATE_HMILY_TRANSACTION_RETRY_LOCK = "update hmily_transaction_global set version =?, retry =? where trans_id = ? and version = ? ";
    
    protected static final String UPDATE_HMILY_PARTICIPANT_LOCK = "update hmily_transaction_participant set version =?, retry =? where participant_id = ? and version = ? ";
    
    protected static final String DELETE_HMILY_TRANSACTION = "delete from hmily_transaction_global where trans_id = ? ";
    
    protected static final String INSERT_HMILY_PARTICIPANT = "INSERT INTO hmily_transaction_participant (participant_id, participant_ref_id, trans_id, trans_type, status, app_name,"
            + "role, retry, target_class, target_method, confirm_method, cancel_method, confirm_invocation, cancel_invocation, version, create_time, update_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? , ? , ?, ?)";
    
    protected static final String SELECTOR_HMILY_PARTICIPANT_COMMON = "select participant_id, participant_ref_id, trans_id, trans_type, status, app_name,"
            + "role, retry, target_class, target_method, confirm_method, cancel_method, confirm_invocation, cancel_invocation, version from hmily_transaction_participant ";
    
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_KEY = SELECTOR_HMILY_PARTICIPANT_COMMON + " where participant_id = ?";
    
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_PARTICIPANT_REF_ID = SELECTOR_HMILY_PARTICIPANT_COMMON + " where participant_ref_id = ?";
    
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_TRANS_ID = SELECTOR_HMILY_PARTICIPANT_COMMON + " where trans_id = ?";
    
    protected static final String EXIST_HMILY_PARTICIPANT_WITH_TRANS_ID = " select count(*) as count_total from hmily_transaction_participant where trans_id = ? ";
    
    protected static final String SELECTOR_HMILY_PARTICIPANT_WITH_DELAY_AND_APP_NAME = SELECTOR_HMILY_PARTICIPANT_COMMON + " where update_time < ? and app_name = ? ";
    
    protected static final String UPDATE_HMILY_PARTICIPANT_STATUS = "update hmily_transaction_participant set status=? where participant_id = ? ";
    
    protected static final String DELETE_HMILY_PARTICIPANT = "delete from hmily_transaction_participant where participant_id = ? ";
    
    /**
     * The data source.
     */
    protected DataSource dataSource;
    
    /**
     * The hmily serializer
     */
    protected HmilySerializer hmilySerializer;
    
    /**
     * The App name.
     */
    protected String appName;
    
    /**
     * Sql file path string.
     *
     * @return the string
     */
    protected abstract String sqlFilePath();
    
    protected abstract String hmilyTransactionLimitSql();
    
    protected abstract String hmilyParticipantLimitSql();
    
    /**
     * Convert data type object.
     *
     * @param params the params
     * @return the object
     */
    protected abstract Object convertDataType(final Object params);
    
    @Override
    public void init(final HmilyConfig hmilyConfig) {
        try {
            HmilyDbConfig hmilyDbConfig = hmilyConfig.getHmilyDbConfig();
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(hmilyDbConfig.getUrl());
            hikariDataSource.setDriverClassName(hmilyDbConfig.getDriverClassName());
            hikariDataSource.setUsername(hmilyDbConfig.getUsername());
            hikariDataSource.setPassword(hmilyDbConfig.getPassword());
            hikariDataSource.setMaximumPoolSize(hmilyDbConfig.getMaxActive());
            hikariDataSource.setMinimumIdle(hmilyDbConfig.getMinIdle());
            hikariDataSource.setConnectionTimeout(hmilyDbConfig.getConnectionTimeout());
            hikariDataSource.setIdleTimeout(hmilyDbConfig.getIdleTimeout());
            hikariDataSource.setMaxLifetime(hmilyDbConfig.getMaxLifetime());
            hikariDataSource.setConnectionTestQuery(hmilyDbConfig.getConnectionTestQuery());
            if (hmilyDbConfig.getDataSourcePropertyMap() != null && !hmilyDbConfig.getDataSourcePropertyMap().isEmpty()) {
                hmilyDbConfig.getDataSourcePropertyMap().forEach(hikariDataSource::addDataSourceProperty);
            }
            this.dataSource = hikariDataSource;
            this.appName = hmilyConfig.getAppName();
            if (hmilyConfig.isAutoSql()) {
                String jdbcUrl = StringUtils.replace(hmilyDbConfig.getUrl(), "/hmily?", "?");
                Connection connection = DriverManager.getConnection(jdbcUrl, hmilyDbConfig.getUsername(), hmilyDbConfig.getPassword());
                this.executeScript(connection, sqlFilePath());
            }
        } catch (Exception e) {
            log.error("hmily jdbc log init exception please check config:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public HmilyTransaction findByTransId(final String transId) {
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
        final Integer currentVersion = hmilyParticipant.getVersion();
        hmilyParticipant.setVersion(hmilyParticipant.getVersion() + 1);
        hmilyParticipant.setRetry(hmilyParticipant.getRetry() + 1);
        return executeUpdate(UPDATE_HMILY_PARTICIPANT_LOCK, hmilyParticipant.getVersion(), hmilyParticipant.getRetry(), hmilyParticipant.getParticipantId(), currentVersion) > 0;
    }
    
    @Override
    public boolean existHmilyParticipantByTransId(String transId) {
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
    public List<HmilyParticipant> listHmilyParticipantByTransId(String transId) {
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
    public List<HmilyParticipant> listHmilyParticipant(Date date, int limit) {
        String limitSql = hmilyParticipantLimitSql();
        List<Map<String, Object>> participantList = executeQuery(limitSql, date, appName, limit);
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
        String limitSql = hmilyTransactionLimitSql();
        List<Map<String, Object>> list = executeQuery(limitSql, date, appName, limit);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildHmilyTransactionByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    
    @Override
    public int updateHmilyTransactionStatus(final String transId, final Integer status) throws HmilyRepositoryException {
        return executeUpdate(UPDATE_HMILY_TRANSACTION_STATUS, status, transId);
    }
    
    @Override
    public int removeHmilyTransaction(final String transId) {
        return executeUpdate(DELETE_HMILY_TRANSACTION, transId);
    }
    
    @Override
    public int createHmilyParticipant(final HmilyParticipant hmilyParticipant) throws HmilyRepositoryException {
        byte[] confirmSerialize = hmilySerializer.serialize(hmilyParticipant.getConfirmHmilyInvocation());
        byte[] cancelSerialize = hmilySerializer.serialize(hmilyParticipant.getCancelHmilyInvocation());
        return executeUpdate(INSERT_HMILY_PARTICIPANT, hmilyParticipant.getParticipantId(), hmilyParticipant.getParticipantRefId(), hmilyParticipant.getTransId(), hmilyParticipant.getTransType(), hmilyParticipant.getStatus(),
                appName, hmilyParticipant.getRole(), hmilyParticipant.getRetry(), hmilyParticipant.getTargetClass(), hmilyParticipant.getTargetMethod(),
                hmilyParticipant.getConfirmMethod(), hmilyParticipant.getCancelMethod(), confirmSerialize, cancelSerialize, hmilyParticipant.getVersion(), hmilyParticipant.getCreateTime(), hmilyParticipant.getUpdateTime());
    }
    
    
    @Override
    public List<HmilyParticipant> findHmilyParticipant(final String participantId) {
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
    
    private HmilyTransaction buildHmilyTransactionByResultMap(final Map<String, Object> map) {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId((String) map.get("trans_id"));
        hmilyTransaction.setTransType((String) map.get("trans_type"));
        hmilyTransaction.setStatus((Integer) map.get("status"));
        hmilyTransaction.setAppName((String) map.get("app_ame"));
        hmilyTransaction.setRetry((Integer) map.get("retry"));
        hmilyTransaction.setVersion((Integer) map.get("version"));
        return hmilyTransaction;
    }
    
    private HmilyParticipant buildHmilyParticipantByResultMap(final Map<String, Object> map) {
        HmilyParticipant hmilyParticipant = new HmilyParticipant();
        hmilyParticipant.setParticipantId((String) map.get("participant_id"));
        hmilyParticipant.setParticipantRefId((String) map.get("participant_ref_id"));
        hmilyParticipant.setTransId((String) map.get("trans_id"));
        hmilyParticipant.setTransType((String) map.get("trans_type"));
        hmilyParticipant.setStatus((Integer) map.get("status"));
        hmilyParticipant.setRole((Integer) map.get("role"));
        hmilyParticipant.setRetry((Integer) map.get("retry"));
        hmilyParticipant.setAppName((String) map.get("app_name"));
        hmilyParticipant.setTargetClass((String) map.get("target_class"));
        hmilyParticipant.setTargetMethod((String) map.get("target_method"));
        hmilyParticipant.setConfirmMethod((String) map.get("confirm_method"));
        hmilyParticipant.setCancelMethod((String) map.get("cancel_method"));
        byte[] confirmInvocation = (byte[]) map.get("confirm_invocation");
        byte[] cancelInvocation = (byte[]) map.get("cancel_invocation");
        try {
            final HmilyInvocation confirmHmilyInvocation = hmilySerializer.deSerialize(confirmInvocation, HmilyInvocation.class);
            hmilyParticipant.setConfirmHmilyInvocation(confirmHmilyInvocation);
            final HmilyInvocation cancelHmilyInvocation = hmilySerializer.deSerialize(cancelInvocation, HmilyInvocation.class);
            hmilyParticipant.setCancelHmilyInvocation(cancelHmilyInvocation);
        } catch (HmilySerializerException e) {
            log.error("hmilySerializer deSerialize have exception:{} ", e.getMessage());
        }
        hmilyParticipant.setVersion((Integer) map.get("version"));
        return hmilyParticipant;
    }
    
    @Override
    public int updateHmilyParticipantStatus(final String participantId, final Integer status) {
        return executeUpdate(UPDATE_HMILY_PARTICIPANT_STATUS, status, participantId);
    }
    
    
    @Override
    public int removeHmilyParticipant(final String participantId) {
        return executeUpdate(DELETE_HMILY_PARTICIPANT, participantId);
    }
    
    /**
     * Execute update int.
     *
     * @param sql    the sql
     * @param params the params
     * @return the int
     */
    protected int executeUpdate(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataType(params[i]));
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("executeUpdate-> " + e.getMessage());
            return FAIL_ROWS;
        } finally {
            close(connection, ps, null);
        }
    }
    
    private List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataType(params[i]));
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = Maps.newHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }
    
    private static void close(final AutoCloseable... closeables) {
        if (null != closeables && closeables.length > 0) {
            for (AutoCloseable closeable : closeables) {
                close(closeable);
            }
        }
    }
    
    private static void close(final AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
    
    private void executeScript(Connection conn, final String sqlPath) throws Exception {
        ScriptRunner runner = new ScriptRunner(conn);
        // doesn't print logger
        runner.setLogWriter(null);
        runner.setAutoCommit(false);
        Resources.setCharset(StandardCharsets.UTF_8);
        Reader read = Resources.getResourceAsReader(sqlPath);
        runner.runScript(read);
        conn.commit();
        runner.closeConnection();
        conn.close();
    }
}
