/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.p6spy.test;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.junit.Test;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * AtomikosTest .
 *
 * @author sixh chenbin
 */
public class AtomikosTest {

    public AtomikosDataSourceBean getDataSource() {
        // 连接池基本属性
        Properties p = new Properties();
        p.setProperty("url", "jdbc:mysql://192.168.3.26:3306/xa_test");
        p.setProperty("user", "root");
        p.setProperty("password", "123456");

        // 使用AtomikosDataSourceBean封装com.mysql.jdbc.jdbc2.optional.MysqlXADataSource
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        //atomikos要求为每个AtomikosDataSourceBean名称，为了方便记忆，这里设置为和dbName相同
        ds.setUniqueResourceName("xa_test");
        ds.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        ds.setXaProperties(p);
        return ds;
    }

    @Test
    public void test001() throws SystemException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        UserTransaction userTransaction = new UserTransactionImp();
        try {
            userTransaction.begin();
            AtomikosDataSourceBean dataSource = getDataSource();
            connection = dataSource.getConnection();
            String sql = "insert into xa_data (name) values ('chenbin')";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
        } finally {
            connection.close();
            preparedStatement.close();
        }
    }
}
