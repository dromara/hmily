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

import com.alibaba.druid.pool.xa.DruidXADataSource;
import org.dromara.hmily.xa.core.UserTransactionImpl;
import org.dromara.hmily.xa.p6spy.HmilyXaP6Datasource;
import org.junit.Test;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * HmilyXaTest .
 *
 * @author sixh chenbin
 */
public class HmilyXaTest {

    @Test
    public void testDataSource() throws SystemException, SQLException {
        Connection connection = null;
        Connection connection2 = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        UserTransaction userTransaction = new UserTransactionImpl();
        try {
            userTransaction.begin();
            DataSource dataSource = getDataSource();
            connection = dataSource.getConnection();
            String sql = "insert into xa_data (name) values ('chenbin')";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();

            DataSource dataSource2 = getDataSource2();
            connection2 = dataSource2.getConnection();
            String sql2 = "insert into xa_data (name) values ('chenbin')";
            preparedStatement2 = connection2.prepareStatement(sql2);
            preparedStatement2.executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            e.printStackTrace();
        } finally {
            connection.close();
            preparedStatement.close();
            connection2.close();
            preparedStatement2.close();
        }
    }

    @Test
    public void test001() throws SystemException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        UserTransaction userTransaction = new UserTransactionImpl();
        try {
            userTransaction.begin();
            DataSource dataSource = getDataSource();
            connection = dataSource.getConnection();
            String sql = "insert into xa_data (name) values ('chenbin')";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            //调用test002()，测试事务嵌套.
            this.test002();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            e.printStackTrace();
        } finally {
            connection.close();
            assert preparedStatement != null;
            preparedStatement.close();
        }
    }

    private void test002() throws SystemException, SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        UserTransaction userTransaction = new UserTransactionImpl();
        try {
            userTransaction.begin();
            DataSource dataSource = getDataSource2();
            connection = dataSource.getConnection();
            String sql = "insert into xa_dataz (name) values ('chenbin')";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            e.printStackTrace();
        } finally {
            connection.close();
            preparedStatement.close();
        }
    }

    private DataSource getDataSource2() {
        DruidXADataSource druidDataSource = new DruidXADataSource();
        druidDataSource.setUrl("jdbc:mysql://192.168.3.18:3306/xa_test");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return new HmilyXaP6Datasource(druidDataSource);
    }

    private DataSource getDataSource() {
        DruidXADataSource druidDataSource = new DruidXADataSource();
        druidDataSource.setUrl("jdbc:mysql://192.168.3.26:3306/xa_test");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return new HmilyXaP6Datasource(druidDataSource);
    }
}
