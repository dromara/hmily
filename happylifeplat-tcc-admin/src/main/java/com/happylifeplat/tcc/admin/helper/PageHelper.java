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

package com.happylifeplat.tcc.admin.helper;

import com.happylifeplat.tcc.admin.page.PageParameter;

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
