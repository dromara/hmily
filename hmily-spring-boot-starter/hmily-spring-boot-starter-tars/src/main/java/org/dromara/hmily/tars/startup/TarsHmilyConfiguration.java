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

package org.dromara.hmily.tars.startup;

import com.qq.tars.client.Communicator;
import org.dromara.hmily.tars.spring.TarsHmilyCommunicatorBeanPostProcessor;
import org.dromara.hmily.tars.spring.TarsHmilyFilterStartupBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * add HmilyCommunicatorBeanPostProcessor and override old old tars's bean post processor.
 *
 * @author tydhot
 */
@Configuration
public class TarsHmilyConfiguration {

    /**
     * add HmilyCommunicatorBeanPostProcessor.
     *
     * @param  communicator communicator
     * @return HmilyCommunicatorBeanPostProcessor
     */
    @Bean
    public TarsHmilyCommunicatorBeanPostProcessor hmilyCommunicatorBeanPostProcessor(final Communicator communicator) {
        return new TarsHmilyCommunicatorBeanPostProcessor(communicator);
    }

    /**
     * add TarsHmilyStartup.
     *
     * @return TarsHmilyStartup
     */
    @Bean
    public TarsHmilyFilterStartupBean tarsHmilyStartupBean() {
        return new TarsHmilyFilterStartupBean();
    }

}
