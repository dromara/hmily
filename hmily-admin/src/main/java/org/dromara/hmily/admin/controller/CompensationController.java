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

package org.dromara.hmily.admin.controller;

import org.dromara.hmily.admin.annotation.Permission;
import org.dromara.hmily.admin.dto.CompensationDTO;
import org.dromara.hmily.admin.page.CommonPager;
import org.dromara.hmily.admin.query.CompensationQuery;
import org.dromara.hmily.admin.service.ApplicationNameService;
import org.dromara.hmily.admin.service.CompensationService;
import org.dromara.hmily.admin.vo.HmilyCompensationVO;
import org.dromara.hmily.common.utils.httpclient.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * transaction log rest controller.
 * @author xiaoyu(Myth)
 */
@RestController
@RequestMapping("/compensate")
public class CompensationController {

    private final CompensationService compensationService;

    private final ApplicationNameService applicationNameService;

    @Value("${compensation.retry.max}")
    private Integer recoverRetryMax;

    @Autowired
    public CompensationController(final CompensationService compensationService,
                                  final ApplicationNameService applicationNameService) {
        this.compensationService = compensationService;
        this.applicationNameService = applicationNameService;
    }

    @Permission
    @PostMapping(value = "/listPage")
    public AjaxResponse listPage(@RequestBody final CompensationQuery recoverQuery) {
        final CommonPager<HmilyCompensationVO> pager =
                compensationService.listByPage(recoverQuery);
        return AjaxResponse.success(pager);
    }

    @PostMapping(value = "/batchRemove")
    @Permission
    public AjaxResponse batchRemove(@RequestBody final CompensationDTO compensationDTO) {
        final Boolean success = compensationService.batchRemove(compensationDTO.getIds(),
                compensationDTO.getApplicationName());
        return AjaxResponse.success(success);

    }

    @PostMapping(value = "/update")
    @Permission
    public AjaxResponse update(@RequestBody final CompensationDTO compensationDTO) {
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
