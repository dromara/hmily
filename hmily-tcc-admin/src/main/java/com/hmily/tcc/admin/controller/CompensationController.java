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

package com.hmily.tcc.admin.controller;

import com.hmily.tcc.admin.annotation.Permission;
import com.hmily.tcc.admin.dto.CompensationDTO;
import com.hmily.tcc.admin.page.CommonPager;
import com.hmily.tcc.admin.query.CompensationQuery;
import com.hmily.tcc.admin.service.ApplicationNameService;
import com.hmily.tcc.admin.service.CompensationService;
import com.hmily.tcc.admin.vo.TccCompensationVO;
import com.hmily.tcc.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>Description: .</p>
 * 事务恢复controller
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/10/18 10:31
 * @since JDK 1.8
 */
@RestController
@RequestMapping("/compensate")
public class CompensationController {


    private final CompensationService compensationService;

    private final ApplicationNameService applicationNameService;

    @Value("${compensation.retry.max}")
    private Integer recoverRetryMax;

    @Autowired
    public CompensationController(CompensationService compensationService, ApplicationNameService applicationNameService) {
        this.compensationService = compensationService;
        this.applicationNameService = applicationNameService;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody CompensationQuery recoverQuery) {
        final CommonPager<TccCompensationVO> pager =
                compensationService.listByPage(recoverQuery);
        return AjaxResponse.success(pager);
    }


    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody CompensationDTO compensationDTO) {

        final Boolean success = compensationService.batchRemove(compensationDTO.getIds(), compensationDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody CompensationDTO compensationDTO) {
        if (recoverRetryMax < compensationDTO.getRetry()) {
            return AjaxResponse.error("重试次数超过最大设置，请您重新设置！");
        }
        final Boolean success = compensationService.updateRetry(compensationDTO.getId(),
                compensationDTO.getRetry(), compensationDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/listAppName")
    @Permission
    public AjaxResponse listAppName() {
        final List<String> list = applicationNameService.list();
        return AjaxResponse.success(list);
    }


}
