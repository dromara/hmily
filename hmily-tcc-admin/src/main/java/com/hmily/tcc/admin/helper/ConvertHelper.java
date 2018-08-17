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

import com.hmily.tcc.admin.vo.TccCompensationVO;
import com.hmily.tcc.common.bean.adapter.CoordinatorRepositoryAdapter;
import com.hmily.tcc.common.utils.DateUtils;

/**
 * ConvertHelper.
 * @author xiaoyu(Myth)
 */
public class ConvertHelper {

    public static TccCompensationVO buildVO(final CoordinatorRepositoryAdapter adapter) {
        TccCompensationVO vo = new TccCompensationVO();
        vo.setTransId(adapter.getTransId());
        vo.setCreateTime(DateUtils.parseDate(adapter.getCreateTime()));
        vo.setRetriedCount(adapter.getRetriedCount());
        vo.setLastTime(DateUtils.parseDate(adapter.getLastTime()));
        vo.setVersion(adapter.getVersion());
        vo.setTargetClass(adapter.getTargetClass());
        vo.setTargetMethod(adapter.getTargetMethod());
        vo.setConfirmMethod(adapter.getConfirmMethod());
        vo.setCancelMethod(adapter.getCancelMethod());
        return vo;
    }

}
