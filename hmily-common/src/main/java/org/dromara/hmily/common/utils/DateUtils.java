/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.dromara.hmily.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * DateUtils.
 *
 * @author xiaoyu
 */
public class DateUtils {

    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    private static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * parseLocalDateTime.
     * out put format:yyyy-MM-dd HH:mm:ss
     *
     * @param str date String
     * @return yyyy-MM-dd HH:mm:ss
     * @see LocalDateTime
     */
    private static LocalDateTime parseLocalDateTime(final String str) {
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(DATE_FORMAT_DATETIME));
    }

    /**
     * Gets date yyyy.
     *
     * @return the date yyyy
     */
    public static Date getDateYYYY() {
        LocalDateTime localDateTime = parseLocalDateTime(getCurrentDateTime());
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * Parse date string.
     *
     * @param date the date
     * @return the string
     */
    public static String parseDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return formatLocalDateTime(localDateTime);
    }

    private static String formatLocalDateTime(final LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DATETIME));
    }

    /**
     * Gets current date time.
     *
     * @return the current date time
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DATETIME));
    }

}
