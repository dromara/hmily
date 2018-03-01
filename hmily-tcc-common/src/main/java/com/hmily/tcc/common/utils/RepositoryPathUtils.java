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

package com.hmily.tcc.common.utils;


import com.hmily.tcc.common.constant.CommonConstant;

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
