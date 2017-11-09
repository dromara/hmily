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

package com.happylifeplat.tcc.common.utils;


import com.happylifeplat.tcc.common.constant.CommonConstant;

/**
 * <p>Description: .</p>
 * 获取资源路径的工具类
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 10:58
 * @since JDK 1.8
 */
public class RepositoryPathUtils {


    public static String buildRedisKey(String keyPrefix, String id) {
        return String.join(":", keyPrefix, id);
    }


    public static String buildFilePath(String applicationName) {
        return String.join("/", CommonConstant.PATH_SUFFIX, applicationName.replaceAll("-", "_"));
    }


    public static  String getFullFileName(String filePath,String id) {
        return String.format("%s/%s", filePath, id);
    }


    public static String buildDbTableName(String applicationName) {
        return CommonConstant.DB_SUFFIX + applicationName.replaceAll("-", "_");
    }


    public static String buildMongoTableName(String applicationName) {
        return CommonConstant.DB_SUFFIX + applicationName.replaceAll("-", "_");
    }

    public static String buildRedisKeyPrefix(String applicationName) {
        return String.format(CommonConstant.RECOVER_REDIS_KEY_PRE, applicationName);
    }

    public static String buildZookeeperPathPrefix(String applicationName) {
        return String.join("-", CommonConstant.PATH_SUFFIX, applicationName);
    }


    public static String buildZookeeperRootPath(String prefix, String id) {
        return String.join("/", prefix, id);
    }

}
