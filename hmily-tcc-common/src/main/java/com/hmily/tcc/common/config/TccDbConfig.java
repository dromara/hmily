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
package com.hmily.tcc.common.config;


import lombok.Data;

/**
 * @author xiaoyu
 */
@Data
public class TccDbConfig {

    /**
     * Mysql 驱动
     */
    private String driverClassName = "com.mysql.jdbc.Driver";

    /**
     * url
     */
    private String url;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;


    /**
     * 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
     */
    private int initialSize = 10;


    /**
     * 最大连接池数量
     */
    private int maxActive = 100;

    /**
     * 最小连接池数量
     */
    private int minIdle = 20;


    /**
     * 配置获取连接等待超时的时间
     */
    private int maxWait = 60000;


    /**
     * 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
     */
    private int timeBetweenEvictionRunsMillis = 60000;


    /**
     * 配置一个连接在池中最小生存的时间，单位是毫秒
     */
    private int minEvictableIdleTimeMillis = 300000;


    private String validationQuery = " SELECT 1 ";


    /**
     * 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
     */
    private Boolean testOnBorrow = false;


    /**
     * 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
     */
    private Boolean testOnReturn = false;


    /**
     * 建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，
     * 如果空闲时间大于timeBetweenEvictionRunsMillis，
     * 执行validationQuery检测连接是否有效
     */
    private Boolean testWhileIdle = true;


    /**
     * 是否缓存preparedStatement，也就是PSCache。
     * PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭
     */
    private Boolean poolPreparedStatements=false;


    /**
     * 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
     * 在Druid中，
     * 不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
     */
    private int maxPoolPreparedStatementPerConnectionSize=100;


}
