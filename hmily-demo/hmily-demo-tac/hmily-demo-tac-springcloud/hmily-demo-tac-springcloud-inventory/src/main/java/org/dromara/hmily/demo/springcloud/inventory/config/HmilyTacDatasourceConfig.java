/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.demo.springcloud.inventory.config;

import com.zaxxer.hikari.HikariDataSource;
import org.dromara.hmily.tac.p6spy.HmilyP6Datasource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * The type Hmily tac datasource config.
 *
 * @author zhangzhi
 */
@Configuration
public class HmilyTacDatasourceConfig {
    
    private final DataSourceProperties dataSourceProperties;
    
    /**
     * Instantiates a new Hmily tac datasource config.
     *
     * @param dataSourceProperties the data source properties
     */
    public HmilyTacDatasourceConfig(final DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }
    
    /**
     * Data source data source.
     *
     * @return the data source
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(dataSourceProperties.getUrl());
        hikariDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        hikariDataSource.setUsername(dataSourceProperties.getUsername());
        hikariDataSource.setPassword(dataSourceProperties.getPassword());
        hikariDataSource.setMaximumPoolSize(20);
        hikariDataSource.setMinimumIdle(10);
        hikariDataSource.setConnectionTimeout(30000);
        hikariDataSource.setIdleTimeout(600000);
        hikariDataSource.setMaxLifetime(1800000);
        return new HmilyP6Datasource(hikariDataSource);
    }
}
