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

package org.dromara.hmily.admin.helper;

import org.dromara.hmily.admin.page.PageParameter;

/**
 * ConvertHelper.
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class PageHelper {

    /**
     * build  PageParameter.
     *
     * @param pageParameter pageParameter
     * @param totalCount    totalCount
     * @return {@linkplain PageParameter}
     */
    public static PageParameter buildPage(final PageParameter pageParameter, final int totalCount) {
        final int currentPage = pageParameter.getCurrentPage();
        pageParameter.setTotalCount(totalCount);
        int totalPage = totalCount / pageParameter.getPageSize()
                + ((totalCount % pageParameter.getPageSize() == 0) ? 0 : 1);
        pageParameter.setTotalPage(totalPage);
        pageParameter.setPrePage(currentPage - 1);
        pageParameter.setNextPage(currentPage + 1);
        return pageParameter;
    }


    /**
     * sqlserver page.
     *
     * @param sql  sql
     * @param page page
     * @return String
     */
    public static StringBuilder buildPageSqlForSqlserver(final String sql, final PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(" order by 1");
        pageSql.append(" offset ")
                .append(start)
                .append(" rows fetch next ")
                .append(page.getPageSize())
                .append(" rows only ");
        return pageSql;
    }


    /**
     * mysql build page sql.
     *
     * @param sql  sql
     * @param page page
     * @return String
     */
    public static StringBuilder buildPageSqlForMysql(final String sql, final PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(" limit ").append(start).append(",").append(page.getPageSize());
        return pageSql;
    }

    /**
     * oracle page sql.
     *
     * @param sql  sql
     * @param page page
     * @return String
     */
    public static StringBuilder buildPageSqlForOracle(final String sql, final PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        String end = String.valueOf(page.getCurrentPage() * page.getPageSize());
        pageSql.append("select * from ( select temp.*, rownum row_id from ( ");
        pageSql.append(sql).append(" ) temp where rownum <= ").append(end);
        pageSql.append(" ) where row_id > ").append(start);
        return pageSql;
    }

    public static StringBuilder buildPageSqlForPostgreSQL(final String sql, final PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(" limit ").append(page.getPageSize()).append(" offset ").append(start);
        return pageSql;
    }

}
