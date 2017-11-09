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
package com.happylifeplat.tcc.common.config;


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
