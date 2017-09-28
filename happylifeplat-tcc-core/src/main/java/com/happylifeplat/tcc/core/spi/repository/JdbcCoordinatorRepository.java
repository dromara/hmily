/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.happylifeplat.tcc.core.spi.repository;

import com.alibaba.druid.pool.DruidDataSource;
import com.happylifeplat.tcc.common.config.TccConfig;
import com.happylifeplat.tcc.common.config.TccDbConfig;
import com.happylifeplat.tcc.core.bean.entity.Participant;
import com.happylifeplat.tcc.core.bean.entity.TccTransaction;
import com.happylifeplat.tcc.common.enums.RepositorySupportEnum;
import com.happylifeplat.tcc.common.exception.TccException;
import com.happylifeplat.tcc.common.exception.TccRuntimeException;
import com.happylifeplat.tcc.core.helper.SqlHelper;
import com.happylifeplat.tcc.core.spi.ObjectSerializer;
import com.happylifeplat.tcc.core.spi.CoordinatorRepository;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class JdbcCoordinatorRepository implements CoordinatorRepository {


    private Logger logger = LoggerFactory.getLogger(JdbcCoordinatorRepository.class);

    private DruidDataSource dataSource;


    private String tableName;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(TccTransaction tccTransaction) {
        String sql = "insert into " + tableName + "(trans_id,retried_count,create_time,last_time,version,status,invocation,role,pattern)" +
                " values(?,?,?,?,?,?,?,?,?)";
        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, tccTransaction.getTransId(), tccTransaction.getRetriedCount(), tccTransaction.getCreateTime(), tccTransaction.getLastTime(),
                    tccTransaction.getVersion(), tccTransaction.getStatus(), serialize, tccTransaction.getRole(), tccTransaction.getPattern());

        } catch (TccException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int remove(String id) {
        String sql = "delete from " + tableName + " where trans_id = ? ";
        return executeUpdate(sql, id);
    }

    /**
     * 更新数据
     *
     * @param tccTransaction 事务对象
     * @return rows 1 成功 0 失败 失败需要抛异常
     */
    @Override
    public int update(TccTransaction tccTransaction) {

        final Integer currentVersion = tccTransaction.getVersion();
        tccTransaction.setLastTime(new Date());
        tccTransaction.setVersion(tccTransaction.getVersion() + 1);

        String sql = "update " + tableName +
                " set last_time = ?,version =?,retried_count =?,invocation=?,status=? , pattern=? where trans_id = ? and version=? ";

        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, tccTransaction.getLastTime(),
                    tccTransaction.getVersion(), tccTransaction.getRetriedCount(), serialize,
                    tccTransaction.getStatus(), tccTransaction.getPattern(),
                    tccTransaction.getTransId(), currentVersion);

        } catch (TccException e) {
            e.printStackTrace();
            return 0;
        }


    }


    /**
     * 根据id获取对象
     *
     * @param id 主键id
     * @return TccTransaction
     */
    @Override
    public TccTransaction findById(String id) {
        String selectSql = "select * from " + tableName + " where trans_id=?";
        List<Map<String, Object>> list = executeQuery(selectSql, id);
        for (Map<String, Object> map : list) {
            TccTransaction tccTransaction = new TccTransaction();
            tccTransaction.setTransId((String) map.get("trans_id"));
            tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
            tccTransaction.setCreateTime((Date) map.get("create_time"));
            tccTransaction.setLastTime((Date) map.get("last_time"));
            tccTransaction.setVersion((Integer) map.get("version"));
            tccTransaction.setRole((Integer) map.get("role"));
            tccTransaction.setPattern((Integer) map.get("pattern"));
            tccTransaction.setStatus((Integer) map.get("status"));
            byte[] bytes = (byte[]) map.get("invocation");
            try {
                final List<Participant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
                tccTransaction.setParticipants(participants);
            } catch (TccException e) {
                e.printStackTrace();
            }
            return tccTransaction;
        }
        return null;
    }

    /**
     * 获取需要提交的事务
     *
     * @return List<TransactionRecover>
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<TccTransaction> listAll() {
        String selectSql = "select * from " + tableName;
        List<Map<String, Object>> list = executeQuery(selectSql);
        List<TccTransaction> tccTransactions = new ArrayList<>();


        for (Map<String, Object> map : list) {
            TccTransaction tccTransaction = new TccTransaction();

            tccTransaction.setTransId((String) map.get("trans_id"));
            tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
            tccTransaction.setCreateTime((Date) map.get("create_time"));
            tccTransaction.setLastTime((Date) map.get("last_time"));
            tccTransaction.setVersion((Integer) map.get("version"));
            tccTransaction.setRole((Integer) map.get("role"));
            tccTransaction.setPattern((Integer) map.get("pattern"));
            tccTransaction.setStatus((Integer) map.get("status"));

            byte[] bytes = (byte[]) map.get("invocation");
            try {
                final List<Participant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
                tccTransaction.setParticipants(participants);
            } catch (TccException e) {
                e.printStackTrace();
            }
            tccTransactions.add(tccTransaction);
        }
        return tccTransactions;
    }

    /**
     * 获取延迟多长时间后的事务信息,只要为了防止并发的时候，刚新增的数据被执行
     *
     * @param date 延迟后的时间
     * @return List<TccTransaction>
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<TccTransaction> listAllByDelay(Date date) {
        String sb = "select * from " +
                tableName +
                " where last_time <?";

        List<Map<String, Object>> list = executeQuery(sb, date);

        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(map -> {
                        TccTransaction tccTransaction = new TccTransaction();
                        tccTransaction.setTransId((String) map.get("trans_id"));
                        tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
                        tccTransaction.setCreateTime((Date) map.get("create_time"));
                        tccTransaction.setLastTime((Date) map.get("last_time"));
                        tccTransaction.setVersion((Integer) map.get("version"));
                        tccTransaction.setStatus((Integer) map.get("status"));
                        tccTransaction.setRole((Integer) map.get("role"));
                        tccTransaction.setPattern((Integer) map.get("pattern"));
                        byte[] bytes = (byte[]) map.get("invocation");
                        try {
                            final List<Participant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
                            tccTransaction.setParticipants(participants);
                        } catch (TccException e) {
                            e.printStackTrace();
                        }
                        return tccTransaction;
                    }).collect(Collectors.toList());

        }

        return null;
    }

    /**
     * 初始化操作
     *
     * @param modelName 模块名称
     * @param txConfig  配置信息
     */
    @Override
    public void init(String modelName, TccConfig txConfig) {
        dataSource = new DruidDataSource();
        final TccDbConfig txDbConfig = txConfig.getTccDbConfig();
        dataSource.setUrl(txDbConfig.getUrl());
        dataSource.setDriverClassName(txDbConfig.getDriverClassName());
        dataSource.setUsername(txDbConfig.getUsername());
        dataSource.setPassword(txDbConfig.getPassword());
        dataSource.setInitialSize(2);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(0);
        dataSource.setMaxWait(60000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setPoolPreparedStatements(false);
        this.tableName = "tcc_transaction_" + modelName.replaceAll("-", "_");
        executeUpdate(SqlHelper.buildCreateTableSql(txDbConfig.getDriverClassName(), tableName));
    }


    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return RepositorySupportEnum.DB.getSupport();
    }

    private int executeUpdate(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject((i + 1), params[i]);
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            logger.error("executeUpdate-> " + e.getMessage());
        } finally {
            close(connection, ps, null);
        }
        return 0;
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject((i + 1), params[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            logger.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }

    private void close(Connection connection,
                       PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
