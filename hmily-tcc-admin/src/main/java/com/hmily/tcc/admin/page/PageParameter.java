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

package com.hmily.tcc.admin.page;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/18 15:51
 * @since JDK 1.8
 */
@Data
public class PageParameter implements Serializable {
    private static final long serialVersionUID = -8324693985921606090L;
    public static final int DEFAULT_PAGE_SIZE = 10;
    private int pageSize;
    private int currentPage;
    private int prePage;
    private int nextPage;
    private int totalPage;
    private int totalCount;
    public PageParameter() {
        this.currentPage = 1;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }
    /**
     *
     * @param currentPage 当前页
     * @param pageSize 每页大小
     */
    public PageParameter(int currentPage, int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
    public int getCurrentPage() {
        return currentPage;
    }

}