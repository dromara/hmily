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

package org.dromara.hmily.core.spi.repository;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import org.dromara.hmily.annotation.HmilySPI;
import org.dromara.hmily.common.bean.entity.HmilyParticipant;
import org.dromara.hmily.common.bean.entity.HmilyTransaction;
import org.dromara.hmily.common.config.HmilyConfig;
import org.dromara.hmily.common.config.HmilyDbConfig;
import org.dromara.hmily.common.constant.CommonConstant;
import org.dromara.hmily.common.enums.RepositorySupportEnum;
import org.dromara.hmily.common.exception.HmilyException;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.common.serializer.ObjectSerializer;
import org.dromara.hmily.common.utils.CollectionUtils;
import org.dromara.hmily.common.utils.DbTypeUtils;
import org.dromara.hmily.common.utils.LogUtil;
import org.dromara.hmily.common.utils.RepositoryPathUtils;
import org.dromara.hmily.common.utils.StringUtils;
import org.dromara.hmily.core.helper.SqlHelper;
import org.dromara.hmily.core.spi.HmilyCoordinatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * jdbc impl.
 *
 * @author xiaoyu
 */
@HmilySPI("db")
public class JdbcCoordinatorRepository implements HmilyCoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCoordinatorRepository.class);

    private DataSource dataSource;

    private String tableName;

    private String currentDBType;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(final HmilyTransaction hmilyTransaction) {
        String sql = "insert into " + tableName + "(trans_id,target_class,target_method,retried_count,"
                + "create_time,last_time,version,status,invocation,role,pattern,confirm_method,cancel_method)"
                + " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            final byte[] serialize = serializer.serialize(hmilyTransaction.getHmilyParticipants());
            return executeUpdate(sql, hmilyTransaction.getTransId(), hmilyTransaction.getTargetClass(), hmilyTransaction.getTargetMethod(),
                    hmilyTransaction.getRetriedCount(), hmilyTransaction.getCreateTime(), hmilyTransaction.getLastTime(),
                    hmilyTransaction.getVersion(), hmilyTransaction.getStatus(), serialize, hmilyTransaction.getRole(),
                    hmilyTransaction.getPattern(), hmilyTransaction.getConfirmMethod(), hmilyTransaction.getCancelMethod());
        } catch (HmilyException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int remove(final String id) {
        String sql = "delete from " + tableName + " where trans_id = ? ";
        return executeUpdate(sql, id);
    }

    @Override
    public int update(final HmilyTransaction hmilyTransaction) {
        final Integer currentVersion = hmilyTransaction.getVersion();
        hmilyTransaction.setLastTime(new Date());
        hmilyTransaction.setVersion(hmilyTransaction.getVersion() + 1);
        String sql = "update " + tableName
                + " set last_time = ?,version =?,retried_count =?,invocation=?,status=? ,pattern=? where trans_id = ? and version=? ";
        try {
            final byte[] serialize = serializer.serialize(hmilyTransaction.getHmilyParticipants());
            return executeUpdate(sql, hmilyTransaction.getLastTime(),
                    hmilyTransaction.getVersion(), hmilyTransaction.getRetriedCount(), serialize,
                    hmilyTransaction.getStatus(), hmilyTransaction.getPattern(),
                    hmilyTransaction.getTransId(), currentVersion);
        } catch (HmilyException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int updateParticipant(final HmilyTransaction hmilyTransaction) {
        String sql = "update " + tableName + " set invocation=?  where trans_id = ?  ";
        try {
            final byte[] serialize = serializer.serialize(hmilyTransaction.getHmilyParticipants());
            return executeUpdate(sql, serialize, hmilyTransaction.getTransId());
        } catch (HmilyException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        String sql = "update " + tableName + " set status=?  where trans_id = ?  ";
        return executeUpdate(sql, status, id);
    }

    @Override
    public HmilyTransaction findById(final String id) {
        String selectSql = "select * from " + tableName + " where trans_id=?";
        List<Map<String, Object>> list = executeQuery(selectSql, id);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public List<HmilyTransaction> listAll() {
        String selectSql = "select * from " + tableName;
        List<Map<String, Object>> list = executeQuery(selectSql);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<HmilyTransaction> listAllByDelay(final Date date) {
        String sb = "select * from " + tableName + " where last_time <?";
        List<Map<String, Object>> list = executeQuery(sb, date);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private HmilyTransaction buildByResultMap(final Map<String, Object> map) {
        HmilyTransaction hmilyTransaction = new HmilyTransaction();
        hmilyTransaction.setTransId((String) map.get("trans_id"));
        hmilyTransaction.setRetriedCount((Integer) map.get("retried_count"));
        hmilyTransaction.setCreateTime((Date) map.get("create_time"));
        hmilyTransaction.setLastTime((Date) map.get("last_time"));
        hmilyTransaction.setVersion((Integer) map.get("version"));
        hmilyTransaction.setStatus((Integer) map.get("status"));
        hmilyTransaction.setRole((Integer) map.get("role"));
        hmilyTransaction.setPattern((Integer) map.get("pattern"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
            final List<HmilyParticipant> hmilyParticipants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
            hmilyTransaction.setHmilyParticipants(hmilyParticipants);
        } catch (HmilyException e) {
            e.printStackTrace();
        }
        return hmilyTransaction;
    }

    @Override
    public void init(final String modelName, final HmilyConfig txConfig) {
        try {
            final HmilyDbConfig hmilyDbConfig = txConfig.getHmilyDbConfig();
            if (hmilyDbConfig.getDataSource() != null && StringUtils.isBlank(hmilyDbConfig.getUrl())) {
                dataSource = hmilyDbConfig.getDataSource();
            } else {
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
                dataSource = hikariDataSource;
            }
            this.tableName = RepositoryPathUtils.buildDbTableName(modelName);
            //save current database type
            this.currentDBType = DbTypeUtils.buildByDriverClassName(hmilyDbConfig.getDriverClassName());
            executeUpdate(SqlHelper.buildCreateTableSql(hmilyDbConfig.getDriverClassName(), tableName));
        } catch (Exception e) {
            LogUtil.error(LOGGER, "hmily jdbc log init exception please check config:{}", e::getMessage);
            throw new HmilyRuntimeException(e);
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.DB.getSupport();
    }

    private int executeUpdate(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataTypeToDB(params[i]));
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("executeUpdate-> " + e.getMessage());
            return FAIL_ROWS;
        } finally {
            close(connection, ps, null);
        }

    }

    private Object convertDataTypeToDB(final Object params) {
        //https://jdbc.postgresql.org/documentation/head/8-date-time.html
        if (CommonConstant.DB_POSTGRESQL.equals(currentDBType) && params instanceof java.util.Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) params).getTime()), ZoneId.systemDefault());
        }
        return params;
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
                    ps.setObject(i + 1, convertDataTypeToDB(params[i]));
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
            LOGGER.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }

    private void close(final Connection con, final PreparedStatement ps, final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        try {
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }
}
