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

package org.dromara.hmily.annotation;

/**
 * PropagationEnum.
 * @author xiaoyu
 */

public enum PropagationEnum {

    /**
     * PropagationEnum required propagation.
     */
    PROPAGATION_REQUIRED(0),

    /**
     * PropagationEnum supports propagation.
     */
    PROPAGATION_SUPPORTS(1),

    /**
     * PropagationEnum mandatory propagation.
     */
    PROPAGATION_MANDATORY(2),

    /**
     * PropagationEnum requires new propagation.
     */
    PROPAGATION_REQUIRES_NEW(3),

    /**
     * PropagationEnum not supported propagation.
     */
    PROPAGATION_NOT_SUPPORTED(4),

    /**
     * PropagationEnum never propagation.
     */
    PROPAGATION_NEVER(5),

    /**
     * PropagationEnum nested propagation.
     */
    PROPAGATION_NESTED(6);


    private final int value;

    PropagationEnum(int value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue() {
        return this.value;
    }


}
