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
public class TccMongoConfig {

    /**
     * mongo数据库设置
     */
    private String mongoDbName;

    /**
     * mongo数据库URL
     */
    private String mongoDbUrl;
    /**
     * mongo数据库用户名
     */
    private String mongoUserName;

    /**
     * mongo数据库密码
     */
    private String mongoUserPwd;

}
