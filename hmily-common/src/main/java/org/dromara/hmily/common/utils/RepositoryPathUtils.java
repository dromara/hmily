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

package org.dromara.hmily.common.utils;

import org.dromara.hmily.common.constant.CommonConstant;

/**
 * The RepositoryPathUtils.
 *
 * @author xiaoyu(Myth)
 */
public class RepositoryPathUtils {

    /**
     * Build redis key string.
     *
     * @param keyPrefix the key prefix
     * @param id        the id
     * @return the string
     */
    public static String buildRedisKey(final String keyPrefix, final String id) {
        return String.join(":", keyPrefix, id);
    }

    /**
     * Build file path string.
     *
     * @param applicationName the application name
     * @return the string
     */
    public static String buildFilePath(final String applicationName) {
        return String.join("/", CommonConstant.PATH_SUFFIX, applicationName.replaceAll("-", "_"));
    }

    /**
     * Gets full file name.
     *
     * @param filePath the file path
     * @param id       the id
     * @return the full file name
     */
    public static String getFullFileName(final String filePath, final String id) {
        return String.format("%s/%s", filePath, id);
    }

    /**
     * Build db table name string.
     *
     * @param applicationName the application name
     * @return the string
     */
    public static String buildDbTableName(final String applicationName) {
        return CommonConstant.DB_SUFFIX + applicationName.replaceAll("-", "_");
    }

    /**
     * Build mongo table name string.
     *
     * @param applicationName the application name
     * @return the string
     */
    public static String buildMongoTableName(final String applicationName) {
        return CommonConstant.DB_SUFFIX + applicationName.replaceAll("-", "_");
    }

    /**
     * Build redis key prefix string.
     *
     * @param applicationName the application name
     * @return the string
     */
    public static String buildRedisKeyPrefix(final String applicationName) {
        return String.format(CommonConstant.RECOVER_REDIS_KEY_PRE, applicationName);
    }

    /**
     * Build zookeeper path prefix string.
     *
     * @param applicationName the application name
     * @return the string
     */
    public static String buildZookeeperPathPrefix(final String applicationName) {
        return String.join("-", CommonConstant.PATH_SUFFIX, applicationName);
    }

    /**
     * Build zookeeper root path string.
     *
     * @param prefix the prefix
     * @param id     the id
     * @return the string
     */
    public static String buildZookeeperRootPath(final String prefix, final String id) {
        return String.join("/", prefix, id);
    }

}
