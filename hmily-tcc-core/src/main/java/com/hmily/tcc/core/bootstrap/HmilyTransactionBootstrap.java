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

package com.hmily.tcc.core.bootstrap;

import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.core.helper.SpringBeanUtils;
import com.hmily.tcc.core.service.HmilyInitService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * hmily bootstrap.
 * @author xiaoyu
 */
public class HmilyTransactionBootstrap extends TccConfig implements ApplicationContextAware {

    private final HmilyInitService hmilyInitService;

    @Autowired
    public HmilyTransactionBootstrap(final HmilyInitService hmilyInitService) {
        this.hmilyInitService = hmilyInitService;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
        start(this);
    }

    private void start(final TccConfig tccConfig) {
        hmilyInitService.initialization(tccConfig);
    }
}
