/*
 * Copyright 2017-2021 Dromara.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.spring.aop;

import org.dromara.hmily.core.aspect.AbstractHmilyTransactionAspect;
import org.springframework.core.Ordered;

/**
 * The type Spring hmily transaction aspect.
 *
 * @author xiaoyu
 */
public class SpringHmilyTransactionAspect extends AbstractHmilyTransactionAspect implements Ordered {
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
