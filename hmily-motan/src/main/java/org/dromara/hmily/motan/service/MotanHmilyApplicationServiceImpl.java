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

package org.dromara.hmily.motan.service;

import com.weibo.api.motan.config.springsupport.BasicServiceConfigBean;
import org.dromara.hmily.core.service.HmilyApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The motan impl HmilyApplicationService.
 *
 * @author xiaoyu
 */
@Service("hmilyApplicationService")
public class MotanHmilyApplicationServiceImpl implements HmilyApplicationService {

    private final BasicServiceConfigBean basicServiceConfigBean;

    @Autowired
    public MotanHmilyApplicationServiceImpl(final BasicServiceConfigBean basicServiceConfigBean) {
        this.basicServiceConfigBean = basicServiceConfigBean;
    }

    @Override
    public String acquireName() {
        return Optional.ofNullable(basicServiceConfigBean).orElse(build()).getModule();
    }

    private BasicServiceConfigBean build() {
        final BasicServiceConfigBean basicServiceConfigBean = new BasicServiceConfigBean();
        basicServiceConfigBean.setBeanName("hmily-motan");
        return basicServiceConfigBean;
    }
}
