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
public class TccRedisConfig {


    private  Boolean cluster;

    /**
     * 集群url   ip:port;ip:port
     */
    private String clusterUrl;

    private String hostName;
    private int port;
    private String password;
    private int maxTotal = 8;
    private int maxIdle = 8;
    private int minIdle = 0;
    private long maxWaitMillis = -1L;
    private long minEvictableIdleTimeMillis = 1800000L;
    private long softMinEvictableIdleTimeMillis = 1800000L;
    private int numTestsPerEvictionRun = 3;
    private Boolean testOnCreate = false;
    private Boolean testOnBorrow = false;
    private Boolean testOnReturn = false;
    private Boolean testWhileIdle = false;
    private long timeBetweenEvictionRunsMillis = -1L;
    private boolean blockWhenExhausted = true;
    private int timeOut = 10000;

}
