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

package com.hmily.tcc.admin.helper;

import com.hmily.tcc.admin.page.PageParameter;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/19 18:30
 * @since JDK 1.8
 */
public class PageHelper {

    public static PageParameter buildPage(PageParameter pageParameter, int totalCount) {
        final int currentPage = pageParameter.getCurrentPage();
        pageParameter.setTotalCount(totalCount);
        int totalPage = totalCount / pageParameter.getPageSize() +
                ((totalCount % pageParameter.getPageSize() == 0) ? 0 : 1);
        pageParameter.setTotalPage(totalPage);
        pageParameter.setPrePage(currentPage - 1);
        pageParameter.setNextPage(currentPage + 1);
        return pageParameter;
    }


    /**
     * sqlserver的分页语句
     *
     * @param sql
     * @param page
     * @return String
     */
    public static  StringBuilder buildPageSqlForSqlserver(String sql, PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(" order by 1");
        pageSql.append(" offset " + start + " rows fetch next " + page.getPageSize() + " rows only ");
        return pageSql;
    }

    /**
     * sqlserver的分页语句
     *
     * @param sql
     * @param page
     * @return String
     */
    public static StringBuilder buildPageSqlForSqlserver(String sql, PageParameter page, String orderBy) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(orderBy);
        pageSql.append(" offset " + start + " rows fetch next " + page.getPageSize() + " rows only ");
        return pageSql;
    }
    /**
     * mysql的分页语句
     *
     * @param sql
     * @param page
     * @return String
     */
    public static StringBuilder buildPageSqlForMysql(String sql, PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(" limit " + start + "," + page.getPageSize());
        return pageSql;
    }
    /**
     * mysql的分页语句
     *
     * @param sql
     * @param page
     * @return String
     */
    public static StringBuilder buildPageSqlForMysql(String sql, PageParameter page, String orderBy) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        pageSql.append(sql);
        pageSql.append(orderBy);
        pageSql.append(" limit " + start + "," + page.getPageSize());
        return pageSql;
    }
    /**
     * 参考hibernate的实现完成oracle的分页
     *
     * @param sql
     * @param page
     * @return String
     */
    public static StringBuilder buildPageSqlForOracle(String sql, PageParameter page) {
        StringBuilder pageSql = new StringBuilder(100);
        String start = String.valueOf((page.getCurrentPage() - 1) * page.getPageSize());
        String end = String.valueOf(page.getCurrentPage() * page.getPageSize());
        pageSql.append("select * from ( select temp.*, rownum row_id from ( ");
        pageSql.append(sql);
        pageSql.append(" ) temp where rownum <= ").append(end);
        pageSql.append(" ) where row_id > ").append(start);
        return pageSql;
    }

}
